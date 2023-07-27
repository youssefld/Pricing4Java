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
public class YamlParsingTests {

    @Test
    @Order(1)
    void parseYamlToClassTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/models.yml");

        assertTrue(pricingManager.plans.get("BASIC") instanceof Plan, "Should be an instance of PricingManager");
        assertEquals(false, pricingManager.plans.get("BASIC").getFeatures().get("haveCalendar").getDefaultValue(), "The deafult value of the haveCalendar feature should be false");
        assertEquals(2, pricingManager.plans.get("BASIC").getFeatures().get("maxPets").getValue(), "The value of the maxPets feature should be 2, as it must be copied from the defaultValue");
    }

    @Test
    @Order(2)
    void changeYamlTest() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/models.yml");

        pricingManager.plans.get("BASIC").price = 1000.0;

        YamlUtils.writeYaml(pricingManager, "pricing/models.yml");

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        PricingManager newPricingManager = YamlUtils.retrieveManagerFromYaml("pricing/models.yml");
    
        assertEquals(1000.0, newPricingManager.plans.get("BASIC").price, "The price has not being changed on the yaml");
        
        newPricingManager.plans.get("BASIC").price = 0.0;

        YamlUtils.writeYaml(newPricingManager, "pricing/models.yml");

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        assertEquals(0.0, newPricingManager.plans.get("BASIC").price, "The price has not being reset");

    }

}
