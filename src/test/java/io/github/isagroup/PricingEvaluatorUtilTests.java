package io.github.isagroup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.isagroup.services.jwt.JwtUtils;

public class PricingEvaluatorUtilTests {

    private static final String JWT_SECRET_TEST = "secret";
    private static final Integer JWT_EXPIRATION_TEST = 86400;
    private static final String JWT_SUBJECT_TEST = "admin1";
    private static final String JWT_EXPRESSION_TEST = "userContext['pets']*4 < planContext['maxPets']";

    private static Map<String, String> userAuthorities = new HashMap<>();

    private static Map<String, Object> userContext = new HashMap<>();

    private static Map<String, Object> planContext = new HashMap<>();

    private static Map<String, String> evaluationContext = new HashMap<>();

    private static JwtUtils jwtUtil = new JwtUtils(JWT_SECRET_TEST);

    @BeforeAll
    public static void setUp() {

        userAuthorities.put("role", "admin");
        userAuthorities.put("username", "admin1");
        userAuthorities.put("password", "4dm1n");

        userContext.put("username", JWT_SUBJECT_TEST);
        userContext.put("pets", 2);
        userContext.put("haveVetSelection", true);
        userContext.put("haveCalendar", true);
        userContext.put("havePetsDashboard", true);
        userContext.put("haveOnlineConsultations", true);

        evaluationContext.put("maxPets", "userContext['pets'] < planContext['maxPets']");
        evaluationContext.put("maxVisitsPerMonthAndPet", "userContext['pets']*4 < planContext['maxPets']");
        evaluationContext.put("supportPriority", "");
        evaluationContext.put("haveCalendar", "planContext['haveCalendar']");
        evaluationContext.put("havePetsDashboard", "planContext['havePetsDashboard']");
        evaluationContext.put("haveVetSelection", "planContext['haveVetSelection']");
        evaluationContext.put("haveOnlineConsultation", "planContext['haveOnlineConsultation']");
        
        planContext.put("maxPets", 6);
        planContext.put("maxVisitsPerMonthAndPet", 6);
        planContext.put("supportPriority", "");
        planContext.put("haveCalendar", true);
        planContext.put("havePetsDashboard", true);
        planContext.put("haveVetSelection", false);
        planContext.put("haveOnlineConsultation", true);

    }

    @Test
    void simpleTokenGenerationTest() {

        PricingEvaluatorUtil togglingUtil = new PricingEvaluatorUtil(planContext, evaluationContext, userContext,
                userAuthorities, JWT_SECRET_TEST, JWT_EXPIRATION_TEST);

        String token = togglingUtil.generateUserToken();

        Map<String, Map<String, Object>> features = jwtUtil.getFeaturesFromJwtToken(token);

        assertTrue(jwtUtil.validateJwtToken(token), "Token is not valid");
        assertTrue((Boolean) features.get("maxPets").get("eval"), "Features is not a string");
        assertFalse((Boolean) features.get("maxVisitsPerMonthAndPet").get("eval"), "Features is not a string");
        assertTrue((Boolean) features.get("haveCalendar").get("eval"), "haveCalendar evaluation should be true");
        assertFalse((Boolean) features.get("haveVetSelection").get("eval"), "haveVetSelection evaluation should be false");

    }

    @Test
    void checkTokenSubjectTest() {

        PricingEvaluatorUtil togglingUtil = new PricingEvaluatorUtil(planContext, evaluationContext, userContext,
                userAuthorities, JWT_SECRET_TEST, JWT_EXPIRATION_TEST);

        String token = togglingUtil.generateUserToken();

        String jwtSubject = jwtUtil.getSubjectFromJwtToken(token);

        assertTrue(jwtUtil.validateJwtToken(token), "Token is not valid");
        assertEquals(JWT_SUBJECT_TEST, jwtSubject, "The subject has not being correctly set");

    }

    @Test
    void checkTokenTimeoutTest() {

        PricingEvaluatorUtil togglingUtil = new PricingEvaluatorUtil(planContext, evaluationContext, userContext,
                userAuthorities, JWT_SECRET_TEST, 1000);

        String token = togglingUtil.generateUserToken();

        assertTrue(jwtUtil.validateJwtToken(token), "Token is not valid");

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        assertFalse(jwtUtil.validateJwtToken(token), "Token is still being valid after timeout has passed");

    }

    @Test
    void tokenExpressionsTest() {

        PricingEvaluatorUtil togglingUtil = new PricingEvaluatorUtil(planContext, evaluationContext, userContext,
                userAuthorities, JWT_SECRET_TEST, JWT_EXPIRATION_TEST);
                
            
        togglingUtil.addExpressionToToken("maxVisitsPerMonthAndPet", "userContext['pets']*4 < planContext['maxPets']");

        String token = togglingUtil.generateUserToken();

        Map<String, Map<String, Object>> features = jwtUtil.getFeaturesFromJwtToken(token);

        assertTrue(jwtUtil.validateJwtToken(token), "Token is not valid");
        assertEquals(JWT_EXPRESSION_TEST, (String) features.get("maxVisitsPerMonthAndPet").get("eval"), "The expression for the feature maxVisitsPerMonthAndPet has not being correctly set");

    }

    @Test
    void tokenPlanContextTest() {

        PricingEvaluatorUtil togglingUtil = new PricingEvaluatorUtil(planContext, evaluationContext, userContext,
                userAuthorities, JWT_SECRET_TEST, JWT_EXPIRATION_TEST);

        String token = togglingUtil.generateUserToken();

        Map<String, Object> tokenicedPlanContext = jwtUtil.getPlanContextFromJwtToken(token);

        assertTrue(jwtUtil.validateJwtToken(token), "Token is not valid");
        assertEquals((Integer) planContext.get("maxPets"), (Integer) tokenicedPlanContext.get("maxPets"), "PlanContext maxPets value is not the same after token codification");
        assertEquals((Boolean) planContext.get("havePetsDashboard"), (Boolean) tokenicedPlanContext.get("havePetsDashboard"), "PlanContext havePetsDashboard value is not the same after token codification");
        assertEquals((String) planContext.get("supportPriority"), (String) tokenicedPlanContext.get("supportPriority"), "PlanContext havePetsDashboard value is not the same after token codification");

    }

    @Test
    void tokenUserContextTest() {

        PricingEvaluatorUtil togglingUtil = new PricingEvaluatorUtil(planContext, evaluationContext, userContext,
                userAuthorities, JWT_SECRET_TEST, JWT_EXPIRATION_TEST);

        String token = togglingUtil.generateUserToken();

        Map<String, Object> tokenicedUserContext = jwtUtil.getUserContextFromJwtToken(token);

        assertTrue(jwtUtil.validateJwtToken(token), "Token is not valid");
        assertEquals((Integer) userContext.get("pets"), (Integer) tokenicedUserContext.get("pets"), "UserContext pets value is not the same after token codification");
        assertEquals((Boolean) userContext.get("havePetsDashboard"), (Boolean) tokenicedUserContext.get("havePetsDashboard"), "UserContext havePetsDashboard value is not the same after token codification");

    }
}
