package io.github.isagroup.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.yaml.snakeyaml.Yaml;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.parsing.PricingManagerParser;
import io.github.isagroup.services.updaters.Version;
import io.github.isagroup.services.yaml.YamlUtils;

public class PricingManagerParserTest {

    @Test
    void givenPetclinicShouldGetPricingManager() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/petclinic.yml");

        assertTrue(pricingManager.getPlans().get("BASIC") instanceof Plan,
                "Should be an instance of PricingManager");
        assertEquals(false,
                pricingManager.getPlans().get("BASIC").getFeatures().get("haveCalendar")
                        .getDefaultValue(),
                "The deafult value of the haveCalendar feature should be false");
        assertEquals(null, pricingManager.getPlans().get("BASIC").getFeatures().get("maxPets").getValue(),
                "The value of the maxPets should be null");

    }

    @ParameterizedTest
    @CsvSource({ "null-version-defaults-to-1.0", "version-1.0-as-string", "version-1.0-as-float" })
    void givenDifferentFormatsShouldEqualToOneDotZero(String input) {

        Yaml yaml = new Yaml();
        String path = String.format("src/test/resources/parsing/%s.yml", input);
        try {
            Map<String, Object> configFile = yaml
                    .load(new FileInputStream(path));
            PricingManager pricingManager = PricingManagerParser.parseMapToPricingManager(configFile);
            assertEquals(io.github.isagroup.services.updaters.Version.V1_0, pricingManager.getVersion());
        } catch (FileNotFoundException e) {
            fail(String.format("The file with location '%s' was not found", path));
        } catch (PricingParsingException e) {
            fail("Pricing could not be parsed correctly check the yaml file.");
        }
    }

    @Test
    void givenDateInYamlCreatedAtShouldConvertToLocalDateJava() {
        Yaml yaml = new Yaml();
        String path = "src/test/resources/parsing/date-to-localdate-in-createdAt.yml";
        try {
            Map<String, Object> configFile = yaml
                    .load(new FileInputStream(path));
            PricingManager pricingManager = PricingManagerParser.parseMapToPricingManager(configFile);
            assertEquals(LocalDate.of(2024, 8, 30), pricingManager.getCreatedAt());
        } catch (FileNotFoundException e) {
            fail(String.format("The file with location '%s' was not found", path));
        } catch (PricingParsingException e) {
            fail("Pricing could not be parsed correctly check the yaml file.");
        }
    }

    @ParameterizedTest
    @CsvSource({ "version-1.1-as-string", "version-1.1-as-float" })
    void givenDifferentFormatsShouldEqualToOneDotOne(String input) {

        Yaml yaml = new Yaml();
        String path = String.format("src/test/resources/parsing/%s.yml", input);
        try {
            Map<String, Object> configFile = yaml
                    .load(new FileInputStream(path));
            PricingManager pricingManager = PricingManagerParser.parseMapToPricingManager(configFile);
            assertEquals(Version.V1_1, pricingManager.getVersion());
            assertEquals(LocalDate.of(2024, 8, 30), pricingManager.getCreatedAt());

            // 1704110400000 milliseconds => 2024-01-01 12:00:00
            assertEquals(new Date(1704110400000L), pricingManager.getStarts());

            // 1735732800000 milliseconds => 2025-01-01 12:00:00
            assertEquals(new Date(1735732800000L), pricingManager.getEnds());
        } catch (FileNotFoundException e) {
            fail(String.format("The file with location '%s' was not found", path));
        } catch (PricingParsingException e) {
            fail("Pricing could not be parsed correctly check the yaml file.");
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/exceptions.csv", delimiter = ';', useHeadersInDisplayName = true, numLinesToSkip = 1)
    void givenCSVOfYamlShouldThrowParsingExceptions(String input, String expectedErrorMessage) {

        Yaml yaml = new Yaml();
        String path = String.format("src/test/resources/parsing/%s.yml", input);
        try {
            Map<String, Object> configFile = yaml
                    .load(new FileInputStream(path));
            PricingManagerParser.parseMapToPricingManager(configFile);
            fail();
        } catch (FileNotFoundException e) {
            fail(String.format("The file with location '%s' was not found", path));
        } catch (PricingParsingException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

}
