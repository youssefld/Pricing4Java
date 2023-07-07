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
    private Map<String, Object> claims = new HashMap<>();
    private String subject = "Default";

    public PricingEvaluatorUtil(Map<String, Object> planContext, Map<String, String> evaluationConext, Map<String, Object> userContext,
            Object userAuthorities) {
        this.planContext = planContext;
        this.evaluationConext = evaluationConext;
        this.userContext = userContext;
        this.jwtSecret = "jwtSecret";
        this.jwtExpirationMs = 86400000;
        this.userAuthorities = userAuthorities;

        configureTokenParameters();
    }

    public PricingEvaluatorUtil(Map<String, Object> planContext, Map<String, String> evaluationConext, Map<String, Object> userContext,
            Object userAuthorities, String jwtSecret) {
        this.planContext = planContext;
        this.evaluationConext = evaluationConext;
        this.userContext = userContext;
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = 86400000;
        this.userAuthorities = userAuthorities;

        configureTokenParameters();
    }

    public PricingEvaluatorUtil(Map<String, Object> planContext, Map<String, String> evaluationConext, Map<String, Object> userContext,
            Object userAuthorities, String jwtSecret, int jwtExpirationMs) {
        this.planContext = planContext;
        this.evaluationConext = evaluationConext;
        this.userContext = userContext;
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        this.userAuthorities = userAuthorities;

        configureTokenParameters();
    }

    public String generateUserToken() {

        return Jwts.builder()
                .setClaims(this.claims)
                .setSubject(this.subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + this.jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, this.jwtSecret)
                .compact();
    }

    public void addExpressionToToken(String featureId, String expression) {
        Map<String, Object> features = (Map<String, Object>) this.claims.get("features");
        try{
            Map<String, Object> feature = (Map<String, Object>) features.get(featureId);
            feature.put("eval", expression);
        }catch(Exception e){
            logger.warning("Feature not found");
        }
    }

    private void configureTokenParameters(){
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
                Boolean eval = parser.parseExpression(expression).getValue(context, planContextManager,
                        Boolean.class);

                featureStatus.put("eval", eval);

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

        this.claims = claims;

        if (this.userContext.containsKey("username")) {
            this.subject = (String) this.userContext.get("username");
        }else if (this.userContext.containsKey("user")) {
            this.subject = (String) this.userContext.get("user");
        }
    }

}
