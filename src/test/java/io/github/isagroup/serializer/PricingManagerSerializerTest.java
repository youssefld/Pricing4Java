package io.github.isagroup.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.Feature;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.models.UsageLimit;
import io.github.isagroup.models.featuretypes.Domain;
import io.github.isagroup.models.usagelimittypes.Renewable;
import io.github.isagroup.services.parsing.PricingManagerParser;
import io.github.isagroup.services.serializer.OneDotOneSerializer;
import io.github.isagroup.services.serializer.PricingManagerSerializer;
import io.github.isagroup.services.serializer.Serializable;

public class PricingManagerSerializerTest {

    private Yaml yaml;

    @BeforeEach
    public void setUp() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yaml = new Yaml(options);
    }

    private PricingManager initPricingManager() {
        PricingManager pricingManager = new PricingManager();

        pricingManager.setCreatedAt(LocalDate.now());

        Domain domain = new Domain();
        domain.setName("domain");
        domain.setDefaultValue("Bar");
        Map<String, Feature> features = new LinkedHashMap<>();
        features.put("bar", domain);
        pricingManager.setFeatures(features);

        Map<String, UsageLimit> usageLimits = new LinkedHashMap<>();
        Renewable renewable = new Renewable();
        renewable.setName("Renewable");
        renewable.setDefaultValue("Bar");
        usageLimits.put("bar", renewable);
        pricingManager.setUsageLimits(usageLimits);

        Map<String, Plan> plans = new LinkedHashMap<>();
        Plan plan = new Plan();
        plan.setName("BASIC");
        plans.put("BASIC", plan);
        pricingManager.setPlans(plans);

        return pricingManager;
    }

    @Test
    public void givenNoFeaturesShouldThrowException() {

        PricingManager pricingManager = initPricingManager();

        pricingManager.setFeatures(null);
        PricingManagerSerializer pricingManagerSerializer = new PricingManagerSerializer();
        try {
            pricingManagerSerializer.serialize(pricingManager);
            fail("Features are not defined");
        } catch (Exception e) {
            String expected = "Features are null. Filling the pricing with features is mandatory.";
            assertEquals(expected, e.getMessage());
        }

    }

    @Test
    public void givenNoPlansAndAddOnsShouldThrowException() {

        PricingManager pricingManager = initPricingManager();
        pricingManager.setPlans(null);

        PricingManagerSerializer pricingManagerSerializer = new PricingManagerSerializer();
        try {
            pricingManagerSerializer.serialize(pricingManager);
            fail("Plans are not defined");
        } catch (Exception e) {
            String expected = "Plans and AddOns are null. You have to set at least one of them.";
            assertEquals(expected, e.getMessage());
        }

    }

    @Test
    public void givenNoAddOnsShouldSerializeNullAddOns() {

        PricingManager pricingManager = initPricingManager();

        PricingManagerSerializer pricingManagerSerializer = new PricingManagerSerializer();
        try {
            Map<String, Object> res = pricingManagerSerializer.serialize(pricingManager);
            Object actual = res.getOrDefault("addOns", "Fail");
            assertEquals(null, actual, "addOns does not serialized as null value");
        } catch (Exception e) {
            fail("addOns key does not exist");

        }

    }

    @Test
    void givenOneDotZeroShouldUpdateToOneDotOne() {
        String path = "src/test/resources/parsing/version-1.0-as-string.yml";

        Yaml yaml = new Yaml();
        try (FileInputStream fileInput = new FileInputStream(new File(path))) {
            Map<String, Object> configFile = yaml.load(fileInput);
            PricingManager pricingManager = PricingManagerParser.parseMapToPricingManager(configFile);
            Serializable serializer = new OneDotOneSerializer(new PricingManagerSerializer());
            Map<String, Object> res = serializer.serialize(pricingManager);
            assertEquals("1.1", res.get("version"));
            assertEquals("2024-08-31", res.get("createdAt"));
            assertEquals(null, res.get("starts"));
            assertEquals(null, res.get("ends"));

        } catch (IOException e) {
            fail("El archivo no ha sido encontrado");
        } catch (PricingParsingException e) {
            fail("Error al parsear");
        }
    }

    @Test
    void givenOneDotOneShouldSerializeInOneDotOneVersion() {
        String path = "src/test/resources/parsing/version-1.1-as-string.yml";

        Yaml yaml = new Yaml();
        try (FileInputStream fileInput = new FileInputStream(new File(path))) {
            Map<String, Object> configFile = yaml.load(fileInput);
            PricingManager pricingManager = PricingManagerParser.parseMapToPricingManager(configFile);
            Serializable serializer = new OneDotOneSerializer(new PricingManagerSerializer());
            Map<String, Object> res = serializer.serialize(pricingManager);

            assertEquals("1.1", res.get("version"));
            assertEquals(null, res.get("day"));
            assertEquals(null, res.get("month"));
            assertEquals(null, res.get("year"));
            assertEquals("2024-08-30", res.get("createdAt"));
            assertEquals(new Date(1704110400000L), res.get("starts"));
            assertEquals(new Date(1735732800000L), res.get("ends"));

        } catch (IOException e) {
            fail("El archivo no ha sido encontrado");
        } catch (PricingParsingException e) {
            fail("Error al parsear");
        }
    }

}
