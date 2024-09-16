package io.github.isagroup.updaters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.parsing.PricingManagerParser;
import io.github.isagroup.services.serializer.PricingManagerSerializer;
import io.github.isagroup.services.updaters.Updater;
import io.github.isagroup.services.updaters.V11Updater;

public class UpdatersTest {
    @Test
    void givenOneDotZeroShouldUpdateToOneDotOne() {
        String path = "src/test/resources/parsing/version-1.0-as-string.yml";

        Yaml yaml = new Yaml();
        try (FileInputStream fileInput = new FileInputStream(new File(path))) {
            Map<String, Object> configFile = yaml.load(fileInput);
            Updater updater = new V11Updater(configFile);
            try {
                Map<String, Object> res = updater.update();
                assertEquals("1.1", res.get("version"));
                assertEquals("2024-08-31", res.get("createdAt"));
                assertEquals(null, res.get("starts"));
                assertEquals(null, res.get("ends"));
                assertEquals("1==1", ((Map<String, Object>) ((Map<String, Object>) res.get("features")).get("foo"))
                        .get("expression"));
                assertEquals(null, ((Map<String, Object>) ((Map<String, Object>) res.get("features")).get("foo"))
                        .get("serverExpression"));

            } catch (Exception e) {
                fail("Should not end here");
            }

        } catch (IOException e) {
            fail("El archivo no ha sido encontrado");
        }
    }

    @Test
    void givenOneDotOneShouldSerializeInOneDotOneVersion() {
        String path = "src/test/resources/parsing/version-1.1-as-string.yml";

        Yaml yaml = new Yaml();
        try (FileInputStream fileInput = new FileInputStream(new File(path))) {
            Map<String, Object> configFile = yaml.load(fileInput);
            PricingManager pricingManager = PricingManagerParser.parseMapToPricingManager(configFile);
            PricingManagerSerializer serializer = new PricingManagerSerializer();
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
