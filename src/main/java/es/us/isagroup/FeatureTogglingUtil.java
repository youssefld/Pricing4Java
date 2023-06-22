package es.us.isagroup;

import java.util.Map;
import java.util.logging.Logger;
import java.io.File;
import java.util.Date;
import java.util.HashMap;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import com.fasterxml.jackson.databind.ObjectMapper;

class PlanContextManager {
    public Map<String, Object> userContext;
    public Map<String, Object> planContext;
}

public class FeatureTogglingUtil {

    Logger logger = Logger.getLogger(FeatureTogglingUtil.class.getName());

    private String pricingJsonPath;
    private String evaluatorJsonPath;
    private Map<String, Object> userContext;
    private String jwtSecret;
    private int jwtExpirationMs;
    private Object userAuthorities;
    private ObjectMapper mapper;

    public FeatureTogglingUtil(String pricingJsonPath, String evaluatorJsonPath, Map<String, Object> userContext,
            Object userAuthorities) {
        this.pricingJsonPath = pricingJsonPath;
        this.evaluatorJsonPath = evaluatorJsonPath;
        this.userContext = userContext;
        this.jwtSecret = "jwtSecret";
        this.jwtExpirationMs = 86400000;
        this.userAuthorities = userAuthorities;
        this.mapper = new ObjectMapper();
    }

    public FeatureTogglingUtil(String pricingJsonPath, String evaluatorJsonPath, Map<String, Object> userContext,
            String jwtSecret, Object userAuthorities) {
        this.pricingJsonPath = pricingJsonPath;
        this.evaluatorJsonPath = evaluatorJsonPath;
        this.userContext = userContext;
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = 86400000;
        this.userAuthorities = userAuthorities;
        this.mapper = new ObjectMapper();
    }

    public FeatureTogglingUtil(String pricingJsonPath, String evaluatorJsonPath, Map<String, Object> userContext,
            String jwtSecret, int jwtExpirationMs, Object userAuthorities) {
        this.pricingJsonPath = pricingJsonPath;
        this.evaluatorJsonPath = evaluatorJsonPath;
        this.userContext = userContext;
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        this.userAuthorities = userAuthorities;
        this.mapper = new ObjectMapper();
    }

    public String generateUserToken() {

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", this.userAuthorities);

        Map<String, Object> plan = parsePlanFromJson(this.pricingJsonPath);
        Map<String, String> pricingExpressions = parseEvaluatorFromJson(this.evaluatorJsonPath);

        PlanContextManager planContextManager = new PlanContextManager();
        planContextManager.userContext = userContext;
        planContextManager.planContext = plan;

        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
        Map<String, Object> featureMap = new HashMap<>();

        Map<String, Object> featureStatus;

        for (String key : pricingExpressions.keySet()) {

            featureStatus = new HashMap<>();

            String expression = pricingExpressions.get(key);

            if (!expression.trim().equals("")) {
                Boolean eval = parser.parseExpression(expression).getValue(context, planContextManager,
                        Boolean.class);

                featureStatus.put("eval", eval);
            }else{
                featureStatus.put("eval", true);
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

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + this.jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, this.jwtSecret)
                .compact();
    }

    private Map<String, Object> parsePlanFromJson(String pricingJsonPath) {

        Map<String, Object> plan = new HashMap<>();

        File plansJson = new File(pricingJsonPath);
        try {
            plan = this.mapper.readValue(plansJson, Map.class);
        } catch (Exception e) {
            logger.warning("It was not possible to map the pricing json file. Please, check the syntax.");
        }

        return plan;
    }

    private Map<String, String> parseEvaluatorFromJson(String evaluatorJsonPath) {

        Map<String, String> evaluator = new HashMap<>();

        File evaluatorJson = new File(evaluatorJsonPath);
        try {
            evaluator = this.mapper.readValue(evaluatorJson, Map.class);
        } catch (Exception e) {
            logger.warning("It was not possible to map the parsers json file. Please, check the syntax.");
        }

        return evaluator;
    }

}
