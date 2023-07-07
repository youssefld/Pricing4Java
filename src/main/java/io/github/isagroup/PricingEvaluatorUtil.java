package io.github.isagroup;

import java.util.Map;
import java.util.logging.Logger;

import java.util.Date;
import java.util.HashMap;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

class PlanContextManager {
    public Map<String, Object> userContext;
    public Map<String, Object> planContext;
}

public class PricingEvaluatorUtil {

    Logger logger = Logger.getLogger(PricingEvaluatorUtil.class.getName());

    private Map<String, Object> planContext;
    private Map<String, String> evaluationConext;
    private Map<String, Object> userContext;
    private String jwtSecret;
    private int jwtExpirationMs;
    private Object userAuthorities;

    public PricingEvaluatorUtil(Map<String, Object> planContext, Map<String, String> evaluationConext, Map<String, Object> userContext,
            Object userAuthorities) {
        this.planContext = planContext;
        this.evaluationConext = evaluationConext;
        this.userContext = userContext;
        this.jwtSecret = "jwtSecret";
        this.jwtExpirationMs = 86400000;
        this.userAuthorities = userAuthorities;
    }

    public PricingEvaluatorUtil(Map<String, Object> planContext, Map<String, String> evaluationConext, Map<String, Object> userContext,
            Object userAuthorities, String jwtSecret) {
        this.planContext = planContext;
        this.evaluationConext = evaluationConext;
        this.userContext = userContext;
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = 86400000;
        this.userAuthorities = userAuthorities;
    }

    public PricingEvaluatorUtil(Map<String, Object> planContext, Map<String, String> evaluationConext, Map<String, Object> userContext,
            Object userAuthorities, String jwtSecret, int jwtExpirationMs) {
        this.planContext = planContext;
        this.evaluationConext = evaluationConext;
        this.userContext = userContext;
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        this.userAuthorities = userAuthorities;
    }

    public String generateUserToken() {

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", this.userAuthorities);

        PlanContextManager planContextManager = new PlanContextManager();
        planContextManager.userContext = userContext;
        planContextManager.planContext = planContext;

        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
        Map<String, Object> featureMap = new HashMap<>();

        Map<String, Object> featureStatus;

        for (String key : evaluationConext.keySet()) {

            featureStatus = new HashMap<>();

            String expression = evaluationConext.get(key);

            if (!expression.trim().equals("")) {
                String eval = parser.parseExpression(expression).getValue(context, planContextManager,
                        String.class);

                if (eval == null) {
                    featureStatus.put("eval", false);
                }else if(eval.equals("true") || eval.equals("false")){
                    featureStatus.put("eval", Boolean.parseBoolean(eval));
                }else{
                    featureStatus.put("eval", eval);
                }

            }else{
                featureStatus.put("eval", false);
            }

            if (expression.contains("<") || expression.contains(">")) {

                String userContextStatusKey = expression.split("\\[[\\\"|']")[1].split("[\\\"|']\\]")[0].trim();

                featureStatus.put("used", planContextManager.userContext.get(userContextStatusKey));
                featureStatus.put("limit", planContextManager.planContext.get(key));
                
            }else{
                featureStatus.put("used", null);
                featureStatus.put("limit", null);
            }
            
            featureMap.put(key, featureStatus);
        }

        claims.put("features", featureMap);
        claims.put("userContext", planContextManager.userContext);
        claims.put("planContext", planContextManager.planContext);

        String subject = "Default";

        if (this.userContext.containsKey("username")) {
            subject = (String) this.userContext.get("username");
        }else if (this.userContext.containsKey("user")) {
            subject = (String) this.userContext.get("user");
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + this.jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, this.jwtSecret)
                .compact();
    }

}
