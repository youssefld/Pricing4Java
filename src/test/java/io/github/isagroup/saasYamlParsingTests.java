package io.github.isagroup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.yaml.YamlUtils;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class saasYamlParsingTests {
    @Test
    @Order(1)
    void parsePostmanYamlToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/postman.yml");

        assertEquals("Postman", pricingManager.getSaasName(), "The saasName should be Postman");
        assertTrue(pricingManager.getPlans().get("BASIC") instanceof Plan, "Should be an instance of PricingManager");
        assertEquals(true, pricingManager.getPlans().get("BASIC").getFeatures().get("apiClient").getDefaultValue(), "The deafult value of the apiClient feature should be true");
        assertEquals(10000, pricingManager.plans.get("BASIC").getUsageLimits().get("callsToPostmanApi").getDefaultValue(), "The default value of the callsToPostmanApi usageLimit should be 10000, as it must be copied from the defaultValue");
    }

    @Test
    @Order(2)
    void parseFigmaToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/figma.yml");

        assertEquals("Figma", pricingManager.getSaasName(), "The saasName should be Figma");
        assertTrue(pricingManager.getPlans().get("STARTER") instanceof Plan, "Should be an instance of Plan");
        assertEquals(true, pricingManager.getPlans().get("STARTER").getFeatures().get("versionHistory").getDefaultValue(), "The deafult value of the versionHistory feature should be true");
        assertEquals(100, pricingManager.plans.get("STARTER").getUsageLimits().get("teamsLimit").getDefaultValue(), "The default value of the teamsLimit usageLimit should be 100, as it must be copied from the defaultValue");
    }

    @Test
    @Order(3)
    void parseMicrosoftBusinessToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/microsoftEnterprise.yml");

        assertEquals("Microsoft 365 Enterpise", pricingManager.getSaasName(), "The saasName should be Microsoft 365 Enterpise");
        assertTrue(pricingManager.getPlans().get("APPS_FOR_BUSINESS") instanceof Plan, "Should be an instance of Plan");
        assertEquals(false, pricingManager.getPlans().get("APPS_FOR_BUSINESS").getFeatures().get("sharedCalendars").getDefaultValue(), "The deafult value of the sharedCalendars feature should be false");
        assertEquals(300, pricingManager.plans.get("BUSINESS_PREMIUM").getUsageLimits().get("webinarsMaxLimit").getDefaultValue(), "The default value of the teamsLimit usageLimit for the plan BUSINESS_PREMIUM should be 300, as it must be copied from the defaultValue");
    }

    @Test
    @Order(4)
    void parseRapidAPIToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/rapidAPI.yml");

        assertEquals("RapidAPI", pricingManager.getSaasName(), "The saasName should be RapidAPI");
        assertTrue(pricingManager.getPlans().get("BASIC") instanceof Plan, "Should be an instance of Plan");
        assertEquals(true, pricingManager.getPlans().get("BASIC").getFeatures().get("search").getDefaultValue(), "The deafult value of the search feature should be true");
        assertEquals(Double.POSITIVE_INFINITY, pricingManager.plans.get("ENTERPRISE").getUsageLimits().get("numberOfBuilders").getValue(), "The value of the numberOfBuilders usageLimit for the plan ENTERPRISE should be infinite, is it's customizable");
    }

    @Test
    @Order(5)
    void parseSalesCloudToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/salescloud.yml");

        assertEquals("salescloud", pricingManager.getSaasName(), "The saasName should be salescloud");
        assertTrue(pricingManager.getPlans().get("STARTER") instanceof Plan, "Should be an instance of Plan");
        assertEquals(true, pricingManager.getPlans().get("STARTER").getFeatures().get("leadManagement").getDefaultValue(), "The deafult value of the leadManagement feature should be true");
        assertEquals(Double.POSITIVE_INFINITY, pricingManager.plans.get("ENTERPRISE").getUsageLimits().get("numberOfProcessesAndFlows").getValue(), "The value of the numberOfProcessesAndFlows usageLimit for the plan ENTERPRISE should be infinite, is it's customizable");
    }
}
