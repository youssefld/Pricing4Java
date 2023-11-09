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
public class PostmanYamlParsingTests {
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

        assertEquals("Figma", pricingManager.getSaasName(), "The saasName should be Postman");
        assertTrue(pricingManager.getPlans().get("STARTER") instanceof Plan, "Should be an instance of PricingManager");
        assertEquals(true, pricingManager.getPlans().get("STARTER").getFeatures().get("versionHistory").getDefaultValue(), "The deafult value of the versionHistory feature should be true");
        assertEquals(100, pricingManager.plans.get("STARTER").getUsageLimits().get("teamsLimit").getDefaultValue(), "The default value of the teamsLimit usageLimit should be 100, as it must be copied from the defaultValue");
    }
}
