package io.github.isagroup;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.isagroup.models.Feature;
import io.github.isagroup.models.FeatureType;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.yaml.YamlUtils;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import({io.github.isagroup.PricingService.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PricingServiceTests {
    
    private static final String JWT_SECRET_TEST = "secret";
    private static final Integer JWT_EXPIRATION_TEST = 86400;
    private static final String JWT_SUBJECT_TEST = "admin1";

    private static final String TEST_PLAN = "BASIC";
    private static final String TEST_NEW_PLAN = "NEW_PLAN";
    private static Plan newPlan = new Plan();
    private static Feature newFeature = new Feature();
    private static final String TEST_BOOLEAN_ATTRIBUTE = "haveCalendar";
    private static final String TEST_NUMERIC_ATTRIBUTE = "maxPets";
    private static final String TEST_TEXT_ATTRIBUTE = "supportPriority";
    private static final String NEW_FEATURE_TEST_NAME = "newFeature";
    private static final String NEW_FEATURE_TEST_VALUE = "testValue";
    private static final String NEW_FEATURE_TEST_EXPRESSION = "userContext['pets'] > 1";
    private static final PricingManager originalPricingManager = YamlUtils.retrieveManagerFromYaml("pricing/models.yml");
    
    @Configuration
    public static class TestConfiguration {
        
        @Component
        public class PricingContextImpl extends PricingContext {
            
            @Override
            public String getConfigFilePath(){
                return "pricing/models.yml";
            };
            
            @Override
            public String getJwtSecret(){
                return JWT_SECRET_TEST;
            };
            
            @Override
            public int getJwtExpiration(){
                return JWT_EXPIRATION_TEST;
            };

            @Override
            public Map<String, Object> getUserContext() {
                Map<String, Object> userContext = new HashMap<>();
                
                userContext.put("username", JWT_SUBJECT_TEST);
                userContext.put("pets", 2);
                userContext.put("haveVetSelection", true);
                userContext.put("haveCalendar", true);
                userContext.put("havePetsDashboard", true);
                userContext.put("haveOnlineConsultations", true);
                
                return userContext;
            }
        
            @Override
            public String getUserPlan() {
                return "ADVANCED";
            }
        
            @Override
            public Object getUserAuthorities() {
                Map<String, String> userAuthorities = new HashMap<>();
                userAuthorities.put("role", "admin");
                userAuthorities.put("username", "admin1");
                userAuthorities.put("password", "4dm1n");

                return userAuthorities;
            }

        }
        
    }

    @Autowired
    private PricingService pricingService;

    @Autowired
    private PricingContext pricingContext;

    @BeforeAll
    static void setUp(){

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/models.yml");

        newPlan.setDescription("New plan description");
        newPlan.setCurrency("EUR");
        newPlan.setPrice(2.0);
        
        Map<String, Feature> features = pricingManager.getPlans().get(TEST_PLAN).getFeatures();

        Feature newBooleanFeature = features.get(TEST_BOOLEAN_ATTRIBUTE);
        Feature newNumericFeature = features.get(TEST_NUMERIC_ATTRIBUTE);
        Feature newTextFeature = features.get(TEST_TEXT_ATTRIBUTE);

        newBooleanFeature.setValue(true);
        newNumericFeature.setValue(6);
        newTextFeature.setValue("HIGH");

        features.put(TEST_BOOLEAN_ATTRIBUTE, newBooleanFeature);
        features.put(TEST_NUMERIC_ATTRIBUTE, newNumericFeature);
        features.put(TEST_TEXT_ATTRIBUTE, newTextFeature);

        newPlan.setFeatures(features);

        newFeature.setDescription("newFeature description");
        newFeature.setType(FeatureType.TEXT);
        newFeature.setDefaultValue(NEW_FEATURE_TEST_VALUE);
        newFeature.setExpression(NEW_FEATURE_TEST_EXPRESSION);

    }

    // --------------------------- PLAN RETRIEVAL ---------------------------

    @Test
    @Order(10)
    void planRetrievalTest(){

        Plan plan = pricingService.getPlanFromName(TEST_PLAN);

        assertInstanceOf(Plan.class, plan);

        assertEquals(0.0, plan.getPrice());

    }

    @Test
    @Order(20)
    void negativePlanRetrievalTest(){

        String nonExistentPlan = "nonExistentPlan";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Plan plan = pricingService.getPlanFromName(nonExistentPlan);
        });

        assertEquals("The plan " + nonExistentPlan + " does not exist in the current pricing configuration", exception.getMessage());

    }

    // --------------------------- PLAN ADITION ---------------------------

    @Test
    @Order(30)
    void addPlanToConfigurationTest(){

        pricingService.addPlanToConfiguration(TEST_NEW_PLAN, newPlan);

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        assert(pricingManager.getPlans().containsKey(TEST_NEW_PLAN));

    }

    @Test
    @Order(40)
    void negativeAddPlanToConfigurationTest(){

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.addPlanToConfiguration(TEST_PLAN, newPlan);
        });

        assertEquals("The plan " + TEST_PLAN + " already exists in the current pricing configuration", exception.getMessage());

    }

    // --------------------------- PLAN REMOVAL ---------------------------

    @Test
    @Order(50)
    void removePlanFromConfigurationTest(){

        pricingService.removePlanFromConfiguration(TEST_NEW_PLAN);

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        assert(!pricingManager.getPlans().containsKey(TEST_NEW_PLAN));

    }

    @Test
    @Order(60)
    void negativeRemovePlanFromConfigurationTest(){

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.removePlanFromConfiguration(TEST_NEW_PLAN);
        });

        assertEquals("There is no plan with the name " + TEST_NEW_PLAN + " in the current pricing configuration", exception.getMessage());

    }

    // --------------------------- BOOLEAN EDITIONS ---------------------------

    @Test
    @Order(70)
    void planBooleanAttributeEditionTest(){

        Boolean oldValue = false;
        Boolean newValue = true;

        pricingService.setPlanFeatureValue(TEST_PLAN, TEST_BOOLEAN_ATTRIBUTE, newValue);

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        assert(pricingManager.getPlans().get(TEST_PLAN).getFeatures().get(TEST_BOOLEAN_ATTRIBUTE).getValue().equals(newValue));

        pricingService.setPlanFeatureValue(TEST_PLAN, TEST_BOOLEAN_ATTRIBUTE, oldValue);
    }

    @Test
    @Order(80)
    void negativePlanBooleanAttributeEditionTest(){

        Integer newValue = 3;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanFeatureValue(TEST_PLAN, TEST_BOOLEAN_ATTRIBUTE, newValue);
        });

        assertEquals("The value " + newValue.toString() + " is not of the type CONDITION", exception.getMessage());

    }

    @Test
    @Order(90)
    void negativePlanBooleanAttributeEditionTest2(){

        Boolean newValue = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanFeatureValue(TEST_PLAN, TEST_BOOLEAN_ATTRIBUTE, newValue);
        });

        assertEquals("The value " + newValue + " is not of the type CONDITION", exception.getMessage());

    }

    // --------------------------- NUMERIC EDITIONS ---------------------------

    @Test
    @Order(100)
    void planNumericAttributeEditionTest(){

        Integer oldValue = 2;
        Integer newValue = 6;

        pricingService.setPlanFeatureValue(TEST_PLAN, TEST_NUMERIC_ATTRIBUTE, newValue);

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        assert(pricingManager.getPlans().get(TEST_PLAN).getFeatures().get(TEST_NUMERIC_ATTRIBUTE).getValue().equals(newValue));

        pricingService.setPlanFeatureValue(TEST_PLAN, TEST_NUMERIC_ATTRIBUTE, oldValue);
    }

    @Test
    @Order(110)
    void negativePlanNumericAttributeEditionTest(){

        String newValue = "invalidValue";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanFeatureValue(TEST_PLAN, TEST_NUMERIC_ATTRIBUTE, newValue);
        });

        assertEquals("The value " + newValue.toString() + " is not of the type NUMERIC", exception.getMessage());

    }

    @Test
    @Order(120)
    void negativePlanNumericAttributeEditionTest2(){

        Integer newValue = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanFeatureValue(TEST_PLAN, TEST_NUMERIC_ATTRIBUTE, newValue);
        });

        assertEquals("The value " + newValue + " is not of the type NUMERIC", exception.getMessage());

    }

    // --------------------------- TEXT EDITIONS ---------------------------

    @Test
    @Order(130)
    void planTextAttributeEditionTest(){

        String oldValue = "LOW";
        String newValue = "HIGH";

        pricingService.setPlanFeatureValue(TEST_PLAN, TEST_TEXT_ATTRIBUTE, newValue);

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        assert(pricingManager.getPlans().get(TEST_PLAN).getFeatures().get(TEST_TEXT_ATTRIBUTE).getValue().equals(newValue));

        pricingService.setPlanFeatureValue(TEST_PLAN, TEST_TEXT_ATTRIBUTE, oldValue);
    }

    @Test
    @Order(140)
    void negativePlanTextAttributeEditionTest(){

        Integer newValue = 2;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanFeatureValue(TEST_PLAN, TEST_TEXT_ATTRIBUTE, newValue);
        });

        assertEquals("The value " + newValue.toString() + " is not of the type TEXT", exception.getMessage());

    }

    @Test
    @Order(150)
    void negativePlanTextAttributeEditionTest2(){

        String newValue = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanFeatureValue(TEST_PLAN, TEST_TEXT_ATTRIBUTE, newValue);
        });

        assertEquals("The value " + newValue + " is not of the type TEXT", exception.getMessage());

    }

    // --------------------------- EDITIONS OF NONEXISTENT ATTRIBUTES ---------------------------

    @Test
    @Order(160)
    void nonexistentAttributeTest(){

        String unexistentAttribute = "unexistentAttribute";
        Integer newValue = 3;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanFeatureValue(TEST_PLAN, unexistentAttribute, newValue);
        });

        assertEquals("The plan " + TEST_PLAN + " does not have the feature " + unexistentAttribute, exception.getMessage());

    }

    // --------------------------- FEATURES ADITION ---------------------------

    @Test
    @Order(170)
    void addNewFeatureTest(){
        pricingService.addFeatureToConfiguration(NEW_FEATURE_TEST_NAME, newFeature);

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());
    
        assert(pricingManager.getFeatures().containsKey(NEW_FEATURE_TEST_NAME));
        assert(pricingManager.getPlans().get("BASIC").getFeatures().containsKey(NEW_FEATURE_TEST_NAME));
        assert(pricingManager.getPlans().get("ADVANCED").getFeatures().containsKey(NEW_FEATURE_TEST_NAME));
        assert(pricingManager.getPlans().get("PRO").getFeatures().containsKey(NEW_FEATURE_TEST_NAME));
        assertEquals(NEW_FEATURE_TEST_EXPRESSION, pricingManager.getFeatures().get(NEW_FEATURE_TEST_NAME).getExpression());
        assertEquals(NEW_FEATURE_TEST_VALUE, pricingManager.getPlans().get("BASIC").getFeatures().get(NEW_FEATURE_TEST_NAME).getValue());
        assertEquals(NEW_FEATURE_TEST_VALUE, pricingManager.getPlans().get("ADVANCED").getFeatures().get(NEW_FEATURE_TEST_NAME).getValue());
        assertEquals(NEW_FEATURE_TEST_VALUE, pricingManager.getPlans().get("PRO").getFeatures().get(NEW_FEATURE_TEST_NAME).getValue());
    }

    @Test
    @Order(180)
    void negativeAddNewFeatureTest(){

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.addFeatureToConfiguration("haveCalendar", newFeature);
        });

        assertEquals("The feature haveCalendar does already exist in the current pricing configuration. Check the features", exception.getMessage());
    }

    // --------------------------- FEATURES REMOVAL ---------------------------

    @Test
    @Order(190)
    void removeFeatureTest(){
        pricingService.removeFeatureFromConfiguration(NEW_FEATURE_TEST_NAME);

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());
    
        assert(!pricingManager.getFeatures().containsKey(NEW_FEATURE_TEST_NAME));
        assert(!pricingManager.getPlans().get("BASIC").getFeatures().containsKey(NEW_FEATURE_TEST_NAME));
        assert(!pricingManager.getPlans().get("ADVANCED").getFeatures().containsKey(NEW_FEATURE_TEST_NAME));
        assert(!pricingManager.getPlans().get("PRO").getFeatures().containsKey(NEW_FEATURE_TEST_NAME));
    }

    @Test
    @Order(200)
    void negativeRemoveNewFeatureTest(){

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.removeFeatureFromConfiguration(NEW_FEATURE_TEST_NAME);
        });

        assertEquals("There is no feature with the name " + NEW_FEATURE_TEST_NAME + " in the current pricing configuration", exception.getMessage());
    }

    // --------------------------- FEATURES' EXPRESSIONS MANAGEMENT ---------------------------

    @Test
    @Order(210)
    void changeFeatureExpressionTest(){

        pricingService.setFeatureExpression(TEST_NUMERIC_ATTRIBUTE, NEW_FEATURE_TEST_EXPRESSION);

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        assertEquals(NEW_FEATURE_TEST_EXPRESSION, pricingManager.getFeatures().get(TEST_NUMERIC_ATTRIBUTE).getExpression());
    }

    @Test
    @Order(220)
    void negativeChangeFeatureExpressionTest(){

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setFeatureExpression(NEW_FEATURE_TEST_NAME, NEW_FEATURE_TEST_EXPRESSION);
        });

        assertEquals("There is no feature with the name " + NEW_FEATURE_TEST_NAME + " in the current pricing configuration", exception.getMessage());
    }

    // --------------------------- FEATURES' TYPES MANAGEMENT ---------------------------

    @Test
    @Order(230)
    void changeFeatureTypeTest(){

        pricingService.setFeatureType(TEST_NUMERIC_ATTRIBUTE, FeatureType.TEXT);

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        assertEquals(FeatureType.TEXT, pricingManager.getFeatures().get(TEST_NUMERIC_ATTRIBUTE).getType());
    }

    @Test
    @Order(240)
    void negativeChangeFeatureTypeTest(){

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setFeatureType(NEW_FEATURE_TEST_NAME, FeatureType.TEXT);
        });

        assertEquals("There is no feature with the name " + NEW_FEATURE_TEST_NAME + " in the current pricing configuration", exception.getMessage());
    }

    // --------------------------- PLANS' PRICES MANAGEMENT ---------------------------

    @Test
    @Order(250)
    void changePlanPriceTest(){

        pricingService.setPlanPrice(TEST_PLAN, 1000.0);

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        assertEquals(1000.0, pricingManager.getPlans().get(TEST_PLAN).getPrice());
    }

    @Test
    @Order(260)
    void negativeChangePlanPriceTest(){

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanPrice(TEST_NEW_PLAN, 1000.0);
        });

        assertEquals("There is no plan with the name " + TEST_NEW_PLAN + " in the current pricing configuration", exception.getMessage());
    }

    // --------------------------- PRICING CONFIGURATION MANAGEMENT ---------------------------

    @Test
    @Order(270)
    void changePricingConfigurationTest(){
        assertDoesNotThrow(()->{
            pricingService.setPricingConfiguration(originalPricingManager);
        });
    }

    @AfterAll
    static void cleanUp(){
        YamlUtils.writeYaml(originalPricingManager, "pricing/models.yml");
    }

}
