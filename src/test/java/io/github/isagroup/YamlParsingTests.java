package io.github.isagroup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class YamlParsingTests {

    @Test
    @Order(1)
    void parseYamlToClassTest() {

        PricingManager pricingManager = retrieveManagerFromYaml();

        assertTrue(pricingManager.plans.get("BASIC") instanceof Plan, "Should be an instance of PricingManager");
    }

    @Test
    @Order(2)
    void changeYamlTest() {

        PricingManager pricingManager = retrieveManagerFromYaml();

        pricingManager.plans.get("BASIC").price = 1000.0;

        writeYaml(pricingManager);

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        PricingManager newPricingManager = retrieveManagerFromYaml();
    
        assertEquals(1000.0, newPricingManager.plans.get("BASIC").price, "The price has not being changed on the yaml");
        
        newPricingManager.plans.get("BASIC").price = 0.0;

        writeYaml(newPricingManager);

        try{
            Thread.sleep(1500);
        }catch(InterruptedException e){
        }

        assertEquals(0.0, newPricingManager.plans.get("BASIC").price, "The price has not being reset");

    }

    private PricingManager retrieveManagerFromYaml(){
        Yaml yaml = new Yaml(new Constructor(PricingManager.class, new LoaderOptions()));

        PricingManager pricingManager = yaml.load(
            this.getClass().getClassLoader().getResourceAsStream("pricing/models.yml")
        );

        return pricingManager;
    }

    private void writeYaml(PricingManager pricingManager){
        DumperOptions dump = new DumperOptions();
        dump.setIndent(2);
        dump.setPrettyFlow(true);
        dump.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Representer representer = new Representer(dump);
        representer.addClassTag(PricingManager.class, Tag.MAP);
        try {
            Yaml yaml = new Yaml(representer, dump);
            FileWriter writer = new FileWriter("src/test/resources/pricing/models.yml");
            yaml.dump(pricingManager, writer);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
