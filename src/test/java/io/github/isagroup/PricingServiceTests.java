package io.github.isagroup;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.github.isagroup.exceptions.InvalidDefaultValueException;
import io.github.isagroup.exceptions.InvalidValueTypeException;
import io.github.isagroup.models.Feature;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.models.UsageLimit;
import io.github.isagroup.models.UsageLimitType;
import io.github.isagroup.models.ValueType;
import io.github.isagroup.models.featuretypes.Domain;
import io.github.isagroup.models.usagelimittypes.Renewable;
import io.github.isagroup.services.yaml.YamlUtils;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PricingServiceTests {

    private static final String JWT_SECRET_TEST = "secret";
    private static final Integer JWT_EXPIRATION_TEST = 86400;
    private static final String JWT_SUBJECT_TEST = "admin1";
    private static final String PETCLINIC_CONFIG_PATH = "pricing/petclinic.yml";

    private static final String TEMPORAL_CONFIG_PATH = "yaml-testing/temp.yml";
    private boolean removeTempFile = true;

    private static final String TEST_PLAN = "BASIC";
    private static final String TEST_NEW_PLAN = "NEW_PLAN";
    private static Plan newPlan = new Plan();
    private static UsageLimit newUsageLimit = new Renewable();
    private static final String TEST_BOOLEAN_FEATURE = "haveCalendar";
    private static final String TEST_NUMERIC_FEATURE = "maxPets";
    private static final String TEST_TEXT_FEATURE = "supportPriority";
    private static final String NEW_FEATURE_NAME = "newFeature";
    private static final String NEW_FEATURE_TEST_VALUE = "testValue";
    private static final String NEW_FEATURE_TEST_EXPRESSION = "userContext['pets'] > 1";
    private static final PricingManager ORIGINAL_PRICING_MANAGER = YamlUtils
            .retrieveManagerFromYaml(PETCLINIC_CONFIG_PATH);


    private PricingService pricingService;

    private PricingContextTestImpl pricingContextTestImpl;

    @BeforeAll
    static void setUp() {

        // Creation of new plan

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(PETCLINIC_CONFIG_PATH);

        newPlan.setDescription("New plan description");
        newPlan.setMonthlyPrice(2.0);

        Map<String, Feature> features = pricingManager.getPlans().get(TEST_PLAN).getFeatures();

        Feature newBooleanFeature = features.get(TEST_BOOLEAN_FEATURE);
        Feature newNumericFeature = features.get(TEST_NUMERIC_FEATURE);
        Feature newTextFeature = features.get(TEST_TEXT_FEATURE);

        newBooleanFeature.setValue(true);
        newNumericFeature.setValue(6);
        newTextFeature.setValue("HIGH");

        features.put(TEST_BOOLEAN_FEATURE, newBooleanFeature);
        features.put(TEST_NUMERIC_FEATURE, newNumericFeature);
        features.put(TEST_TEXT_FEATURE, newTextFeature);

        newPlan.setFeatures(features);
    }

    @BeforeEach
    public void init() {

        // Read petclinic
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(PETCLINIC_CONFIG_PATH);
        // Write temporal petclinic config file
        YamlUtils.writeYaml(pricingManager, TEMPORAL_CONFIG_PATH);

        Map<String, Object> userContext = new HashMap<>();
        userContext.put("username", JWT_SUBJECT_TEST);
        userContext.put("pets", 2);

        Map<String, Object> userAuthorities = new HashMap<>();
        userAuthorities.put("role", "admin");
        userAuthorities.put("username", JWT_SUBJECT_TEST);
        userAuthorities.put("password", "4dm1n");

        PricingContextTestImpl pricingContextTest = new PricingContextTestImpl();
        pricingContextTest.setConfigFilePath(TEMPORAL_CONFIG_PATH);
        pricingContextTest.setJwtExpiration(JWT_EXPIRATION_TEST);
        pricingContextTest.setJwtSecret(JWT_SECRET_TEST);
        pricingContextTest.setUserPlan(TEST_PLAN);
        pricingContextTest.setUserAuthorities(userAuthorities);
        pricingContextTest.setUserContext(userContext);

        // Reset of new usage limit

        newUsageLimit.setName("newUsageLimit");
        newUsageLimit.setDescription("New usage limit description");
        newUsageLimit.setValueType(ValueType.NUMERIC);
        newUsageLimit.setDefaultValue(10);
        newUsageLimit.setUnit("appointment");
        newUsageLimit.getLinkedFeatures().add(TEST_BOOLEAN_FEATURE);

        this.pricingContextTestImpl = pricingContextTest;
        this.pricingService = new PricingService(pricingContextTest);
        this.removeTempFile = true;
    }

    @AfterEach
    void after() {
        try {

            if(this.removeTempFile){
                File file = new File("src/test/resources/yaml-testing/temp.yml");
                file.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --------------------------- PLAN RETRIEVAL ---------------------------

    @Test
    @Order(10)
    void should_ReturnPlan_given_PlanName() {

        Plan plan = pricingService.getPlanFromName("BASIC");

        assertInstanceOf(Plan.class, plan);

        assertEquals(0.0, plan.getAnnualPrice());
        assertEquals(0.0, plan.getMonthlyPrice());

    }

    @Test
    @Order(20)
    void should_ThrowException_given_NonExistentPlan() {

        String nonExistentPlan = "nonExistentPlan";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.getPlanFromName(nonExistentPlan);
        });

        assertEquals("The plan " + nonExistentPlan + " does not exist in the current pricing configuration",
                exception.getMessage());

    }

    // // --------------------------- PLAN ADITION ---------------------------

    @Test
    @Order(30)
    void given_plan_should_add_plan_to_config_file() {

        pricingService.addPlanToConfiguration(TEST_NEW_PLAN, newPlan);

        // FIXME
        // READS OLD VALUE
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(TEMPORAL_CONFIG_PATH);

        assertTrue(pricingManager.getPlans().containsKey(TEST_NEW_PLAN),
                "Pricing config does not have NEW_PLAN");

    }

    @Test
    @Order(40)
    void given_duplicate_plan_name_should_throw_exception_when_adding_plan() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.addPlanToConfiguration(TEST_PLAN, newPlan);
        });

        assertEquals("The plan " + TEST_PLAN + " already exists in the current pricing configuration",
                exception.getMessage());

    }

    // // --------------------------- PLAN REMOVAL ---------------------------

    @Test
    @Order(50)
    void given_existing_plan_name_should_delete_plan_from_config() {

        String plan = "BASIC";

        pricingService.removePlanFromConfiguration(plan);

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(TEMPORAL_CONFIG_PATH);

        assertFalse(pricingManager.getPlans().containsKey(plan), "Basic plan was not removed");

    }

    @Test
    @Order(60)
    void given_non_existing_plan_name_should_throw_when_deleting() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.removePlanFromConfiguration(TEST_NEW_PLAN);
        });

        assertEquals("There is no plan with the name " + TEST_NEW_PLAN + " in the current pricing configuration",
                exception.getMessage());

    }

    // // --------------------------- BOOLEAN EDITIONS ---------------------------

    @Test
    @Order(70)
    void given__existing_plan_name_and_feature_should_update_boolean_feature() {

        pricingService.setPlanFeatureValue(TEST_PLAN, TEST_BOOLEAN_FEATURE,
                true);

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContextTestImpl.getConfigFilePath());

        assertEquals(true,
                pricingManager.getPlans().get(TEST_PLAN).getFeatures().get(TEST_BOOLEAN_FEATURE).getValue(),
                "haveCalendar from plan BASIC should be true");

    }

    @Test
    @Order(80)
    void given_existing_plan_and_boolean_feature_should_throw_assigning_numeric() {

        Integer newValue = 3;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanFeatureValue(TEST_PLAN, TEST_BOOLEAN_FEATURE,
                    newValue);
        });

        assertEquals("The value " + newValue + " is not of the type BOOLEAN", exception.getMessage());

    }

    @Test
    @Order(90)
    void given_existing_plan_and_boolean_feature_should_throw_assigning_null() {

        Boolean newValue = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanFeatureValue(TEST_PLAN, TEST_BOOLEAN_FEATURE,
                    newValue);
        });

        assertEquals("The value " + newValue + " is not of the type BOOLEAN",
                exception.getMessage());

    }

    // // --------------------------- NUMERIC EDITIONS ---------------------------

    @Test
    @Order(100)
    void given_existing_plan_and_numeric_feature_should_update() {

        Integer newValue = 6;

        pricingService.setPlanFeatureValue(TEST_PLAN, TEST_NUMERIC_FEATURE,
                newValue);

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContextTestImpl.getConfigFilePath());

        assertEquals(newValue,
                pricingManager.getPlans().get(TEST_PLAN).getFeatures().get(TEST_NUMERIC_FEATURE).getValue());

    }

    @Test
    @Order(110)
    void given_string_should_throw_when_updating_numeric_feature() {

        String newValue = "invalidValue";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanFeatureValue(TEST_PLAN, TEST_NUMERIC_FEATURE,
                    newValue);
        });

        assertEquals("The value " + newValue + " is not of the type NUMERIC", exception.getMessage());

    }

    @Test
    @Order(120)
    void given_null_should_throw_when_updating_numeric_feature() {

        Integer newValue = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanFeatureValue(TEST_PLAN, TEST_NUMERIC_FEATURE,
                    newValue);
        });

        assertEquals("The value " + newValue + " is not of the type NUMERIC",
                exception.getMessage());

    }

    // // --------------------------- TEXT EDITIONS ---------------------------

    @Test
    @Order(130)
    void given_string_should_update_text_feature() {

        String newValue = "HIGH";

        pricingService.setPlanFeatureValue(TEST_PLAN, TEST_TEXT_FEATURE, newValue);

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContextTestImpl.getConfigFilePath());

        assertEquals(newValue,
                pricingManager.getPlans().get(TEST_PLAN).getFeatures().get(TEST_TEXT_FEATURE).getValue());

    }

    @Test
    @Order(140)
    void given_number_should_throw_when_updating_text_feature() {

        Integer newValue = 2;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanFeatureValue(TEST_PLAN, TEST_TEXT_FEATURE, newValue);
        });

        assertEquals("The value " + newValue + " is not of the type TEXT",
                exception.getMessage());

    }

    @Test
    @Order(150)
    void given_null_should_throw_when_updating_text_feature() {

        String newValue = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanFeatureValue(TEST_PLAN, TEST_TEXT_FEATURE, newValue);
        });

        assertEquals("The value " + newValue + " is not of the type TEXT",
                exception.getMessage());

    }

    // ---------------EDITIONS OF NONEXISTENT ATTRIBUTES ------------

    @Test
    @Order(160)
    void given_non_existent_feature_should_throw_IllegalArgumentException() {

        String unexistentAttribute = "unexistentAttribute";
        Integer newValue = 3;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanFeatureValue(TEST_PLAN, unexistentAttribute, newValue);
        });

        assertEquals("The plan " + TEST_PLAN + " does not have the feature " +
                unexistentAttribute, exception.getMessage());

    }

    // // --------------------------- FEATURES ADITION ---------------------------

    @Test
    @Order(170)
    void given_new_feature_should_update_all_plan_values() {

        Domain newFeature = new Domain();
        newFeature.setName(NEW_FEATURE_NAME);
        newFeature.setDefaultValue(NEW_FEATURE_TEST_VALUE);
        newFeature.setValueType(ValueType.TEXT);
        newFeature.setExpression(NEW_FEATURE_TEST_EXPRESSION);

        pricingService.addFeatureToConfiguration(NEW_FEATURE_NAME, newFeature);

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContextTestImpl.getConfigFilePath());

        assertTrue(pricingManager.getFeatures().containsKey(NEW_FEATURE_NAME));
        assertTrue(pricingManager.getPlans().get("BASIC").getFeatures().containsKey(NEW_FEATURE_NAME));
        assertTrue(pricingManager.getPlans().get("ADVANCED").getFeatures().containsKey(NEW_FEATURE_NAME));
        assertTrue(pricingManager.getPlans().get("PRO").getFeatures().containsKey(NEW_FEATURE_NAME));

        assertEquals(NEW_FEATURE_TEST_EXPRESSION,
                pricingManager.getFeatures().get(NEW_FEATURE_NAME).getExpression());
        assertEquals(NEW_FEATURE_TEST_VALUE,
                pricingManager.getPlans().get("BASIC").getFeatures().get(NEW_FEATURE_NAME).getValue());
        assertEquals(NEW_FEATURE_TEST_VALUE,
                pricingManager.getPlans().get("ADVANCED").getFeatures().get(NEW_FEATURE_NAME).getValue());
        assertEquals(NEW_FEATURE_TEST_VALUE,
                pricingManager.getPlans().get("PRO").getFeatures().get(NEW_FEATURE_NAME).getValue());
    }

    @Test
    @Order(180)
    void given_non_existent_feature_should_throw_when_adding_feature() {

        Domain newFeature = new Domain();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.addFeatureToConfiguration("haveCalendar", newFeature);
        });

        assertEquals(
                "The feature haveCalendar does already exist in the current pricing configuration. Check the features",
                exception.getMessage());
    }

    // // --------------------------- FEATURES REMOVAL ---------------------------

    @Test
    @Order(190)
    void given_existent_feature_should_remove_feature() {

        String featureName = "maxPets";

        pricingService.removeFeatureFromConfiguration("maxPets");

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContextTestImpl.getConfigFilePath());

        assertFalse(pricingManager.getFeatures().containsKey(featureName));
        assertFalse(pricingManager.getPlans().get("BASIC").getFeatures().containsKey(featureName));
        assertFalse(pricingManager.getPlans().get("ADVANCED").getFeatures().containsKey(featureName));
        assertFalse(pricingManager.getPlans().get("PRO").getFeatures().containsKey(featureName));
    }

    @Test
    @Order(200)
    void given_non_existent_feature_should_throw_when_deleting() {

        String nonExistentFeatureName = "foo";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.removeFeatureFromConfiguration(nonExistentFeatureName);
        });

        assertEquals(
                "There is no feature with the name " + nonExistentFeatureName + " in the current pricing configuration",
                exception.getMessage());
    }

    // ------ FEATURES' EXPRESSIONS MANAGEMENT -------

    @Test
    @Order(210)
    void given_feature_update_expression() {

        pricingService.setFeatureExpression(TEST_NUMERIC_FEATURE,
                NEW_FEATURE_TEST_EXPRESSION);

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContextTestImpl.getConfigFilePath());

        assertEquals(NEW_FEATURE_TEST_EXPRESSION,
                pricingManager.getFeatures().get(TEST_NUMERIC_FEATURE).getExpression());
    }

    @Test
    @Order(220)
    void given_non_existent_feature_should_throw_when_updating_expression() {

        String nonExistentFeature = "non-existent";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setFeatureExpression(nonExistentFeature, NEW_FEATURE_TEST_EXPRESSION);
        });

        assertEquals(
                "There is no feature with the name " + nonExistentFeature + " in the current pricing configuration",
                exception.getMessage());
    }

    // ------------ FEATURES' TYPES MANAGEMENT ---------------------

    @Test
    @Order(230)
    void given_feature_should_update_value_type_to_text() {

        pricingService.setFeatureValueType("maxPets", ValueType.TEXT);

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContextTestImpl.getConfigFilePath());
        Feature feature = pricingManager.getFeatures().get("maxPets");

        assertEquals(ValueType.TEXT, feature.getValueType());
        assertEquals("", feature.getDefaultValue());
        assertEquals("", feature.getExpression());
        assertEquals("", feature.getServerExpression());

    }

    @Test
    @Order(240)
    void given_feature_should_update_value_type_to_boolean() {

        pricingService.setFeatureValueType("maxPets", ValueType.BOOLEAN);

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContextTestImpl.getConfigFilePath());
        Feature feature = pricingManager.getFeatures().get("maxPets");

        assertEquals(ValueType.BOOLEAN, feature.getValueType());
        assertEquals(false, feature.getDefaultValue());
        assertEquals("", feature.getExpression());
        assertEquals("", feature.getServerExpression());

    }

    @Test
    @Order(250)
    void given_non_existent_feature_should_throw_when_updating_value_type() {

        String nonExistentFeature = "non-existent";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setFeatureValueType(nonExistentFeature, ValueType.TEXT);
        });

        assertEquals(
                "There is no feature with the name " + nonExistentFeature + " in the current pricing configuration",
                exception.getMessage());
    }

    // --------------- PLANS' PRICES MANAGEMENT ----------------

    @Test
    @Order(260)
    void given_price_should_update_monthly_price() {

        pricingService.setPlanPrice(TEST_PLAN, 1000.0);

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContextTestImpl.getConfigFilePath());

        assertEquals(1000.0, pricingManager.getPlans().get(TEST_PLAN).getMonthlyPrice());
    }

    @Test
    @Order(270)
    void negativeChangePlanPriceTest() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pricingService.setPlanPrice(TEST_NEW_PLAN, 1000.0);
        });

        assertEquals("There is no plan with the name " + TEST_NEW_PLAN + " in the current pricing configuration",
                exception.getMessage());
    }

    // --------------- USAGE LIMITS' MANAGEMENT ----------------

    @Test
    @Order(280)
    void shouldAddUsageLimit(){

        pricingService.addUsageLimitToConfiguration(newUsageLimit);    

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContextTestImpl.getConfigFilePath());

        UsageLimit createdUsageLimit = pricingManager.getUsageLimits().get(newUsageLimit.getName());
        
        assertEquals("New usage limit description", createdUsageLimit.getDescription());
        assertEquals(ValueType.NUMERIC, createdUsageLimit.getValueType());
        assertEquals(10, createdUsageLimit.getDefaultValue());
        assertEquals("appointment", createdUsageLimit.getUnit());
        assertTrue(createdUsageLimit.getLinkedFeatures().contains(TEST_BOOLEAN_FEATURE));
    }

    @Test
    @Order(290)
    void shouldUpdateUsageLimit(){

        pricingService.addUsageLimitToConfiguration(newUsageLimit);

        newUsageLimit.setDefaultValue(20);
        newUsageLimit.setUnit("day");
        newUsageLimit.getLinkedFeatures().add(TEST_TEXT_FEATURE);

        pricingService.updateUsageLimitFromConfiguration(newUsageLimit);

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContextTestImpl.getConfigFilePath());

        UsageLimit updatedUsageLimit = pricingManager.getUsageLimits().get(newUsageLimit.getName());
        
        assertEquals("New usage limit description", updatedUsageLimit.getDescription());
        assertEquals(ValueType.NUMERIC, updatedUsageLimit.getValueType());
        assertEquals(20, updatedUsageLimit.getDefaultValue());
        assertEquals("day", updatedUsageLimit.getUnit());
        assertTrue(updatedUsageLimit.getLinkedFeatures().contains(TEST_BOOLEAN_FEATURE));
        assertTrue(updatedUsageLimit.getLinkedFeatures().contains(TEST_TEXT_FEATURE));
    }

    @Test
    @Order(290)
    void negativeShouldUpdateUsageLimit(){

        pricingService.addUsageLimitToConfiguration(newUsageLimit);

        newUsageLimit.setDefaultValue("test");

        InvalidDefaultValueException exception = assertThrows(InvalidDefaultValueException.class, () -> {
            pricingService.updateUsageLimitFromConfiguration(newUsageLimit);
        });

        assertEquals("The usage limit defaultValue must be an integer if valueType is NUMERIC",
                exception.getMessage());
    }

    @Test
    @Order(300)
    void shouldRemoveUsageLimit(){

        pricingService.addUsageLimitToConfiguration(newUsageLimit);

        pricingService.removeUsageLimitFromConfiguration(newUsageLimit.getName());

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContextTestImpl.getConfigFilePath());

        assertFalse(pricingManager.getUsageLimits().containsKey(newUsageLimit.getName()));
    }

    // --------------- PRICING CONFIGURATION MANAGEMENT ---------------

    @Test
    @Order(10080)
    void changePricingConfigurationTest() {
        assertDoesNotThrow(() -> {
            pricingService.setPricingConfiguration(ORIGINAL_PRICING_MANAGER);
        });
    }

}
