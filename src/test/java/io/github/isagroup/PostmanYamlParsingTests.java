package io.github.isagroup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.yaml.YamlUtils;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostmanYamlParsingTests {
    @Test
    @Order(1)
    void parsePostmanYamlToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/postman.yml");

        assertEquals("Postman", pricingManager.getSaasName(), "The saasName should be Postman");

        System.out.println(pricingManager.getFeatures());

        // assertTrue(pricingManager.plans.get("BASIC") instanceof Plan, "Should be an instance of PricingManager");
        // assertEquals(false, pricingManager.plans.get("BASIC").getFeatures().get("haveCalendar").getDefaultValue(), "The deafult value of the haveCalendar feature should be false");
        // assertEquals(2, pricingManager.plans.get("BASIC").getFeatures().get("maxPets").getValue(), "The value of the maxPets feature should be 2, as it must be copied from the defaultValue");
    }
}
