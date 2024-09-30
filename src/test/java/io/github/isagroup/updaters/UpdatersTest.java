package io.github.isagroup.updaters;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import io.github.isagroup.exceptions.VersionException;
import io.github.isagroup.services.updaters.Version;
import io.github.isagroup.services.updaters.YamlUpdater;
import io.github.isagroup.services.yaml.YamlUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.yaml.snakeyaml.Yaml;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.parsing.PricingManagerParser;
import io.github.isagroup.services.serializer.PricingManagerSerializer;

import static org.junit.jupiter.api.Assertions.*;

public class UpdatersTest {
    @Test
    void givenOneDotZeroShouldUpdateToOneDotOne() {
        String path = "src/test/resources/updating/v10-v11/v10.yml";

        Yaml yaml = new Yaml();
        try (FileInputStream fileInput = new FileInputStream(path)) {
            Map<String, Object> configFile = yaml.load(fileInput);
            try {
                Map<String, Object> res = YamlUpdater.update(configFile, Version.V1_1);
                assertEquals("1.1", res.get("version"));
                assertEquals("2024-08-31", res.get("createdAt"));
                assertNull(res.get("starts"));
                assertNull(res.get("ends"));
                assertEquals("1==1", ((Map<String, Object>) ((Map<String, Object>) res.get("features")).get("foo"))
                    .get("expression"));
                assertNull(((Map<String, Object>) ((Map<String, Object>) res.get("features")).get("foo"))
                    .get("serverExpression"));

            } catch (Exception e) {
                fail("Should not end here");
            }

        } catch (IOException e) {
            fail("El archivo no ha sido encontrado");
        }
    }

    @Test
    void givenOutOfBoundsMinorShouldThrow() {

        try {
            Version.version("1.9999999999");
        } catch (VersionException e) {
            assertEquals("Unable to parse minor \"9999999999\"", e.getMessage());
        }
    }

    @Test
    void givenOneDotOneYamlShouldReturnSameYaml() {
        Yaml yaml = new Yaml();
        try (FileInputStream f = new FileInputStream("src/test/resources/parsing/version-1.1-as-float.yml")) {
            Map<String, Object> expectConfigFile = yaml.load(f);
            assertEquals(expectConfigFile, YamlUpdater.update(expectConfigFile, Version.V1_1));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void givenInvalidVersionFormatShouldThrow() {
        try {
            Version.version("This is an invalid version :(");
        } catch (VersionException e) {
            assertEquals("Invalid character \"T\" at position 0 in version \"This is an invalid version :(\"", e.getMessage());
        }
    }

    @Test
    void givenOneDotOneShouldSerializeInOneDotOneVersion() {
        String path = "src/test/resources/parsing/version-1.1-as-string.yml";

        Yaml yaml = new Yaml();
        try (FileInputStream fileInput = new FileInputStream(path)) {
            Map<String, Object> configFile = yaml.load(fileInput);
            PricingManager pricingManager = PricingManagerParser.parseMapToPricingManager(configFile);
            PricingManagerSerializer serializer = new PricingManagerSerializer();
            Map<String, Object> res = serializer.serialize(pricingManager);

            assertEquals("1.1", res.get("version"));
            assertNull(res.get("day"));
            assertNull(res.get("month"));
            assertNull(res.get("year"));
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
