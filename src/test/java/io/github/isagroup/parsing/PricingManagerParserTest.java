package io.github.isagroup.parsing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Date;

import io.github.isagroup.exceptions.FilepathException;
import io.github.isagroup.exceptions.InvalidPlanException;
import io.github.isagroup.exceptions.VersionException;
import io.github.isagroup.services.updaters.YamlUpdater;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.yaml.snakeyaml.Yaml;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.parsing.PricingManagerParser;
import io.github.isagroup.services.updaters.Version;
import io.github.isagroup.services.yaml.YamlUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    @ValueSource( strings = { "version-as-string", "version-as-float" })
    void givenDifferentFormatsShouldEqualToOneDotZero(String input) {

        String path = String.format("parsing/pricing-manager/version/positive/%s.yml", input);
        try {
           YamlUtils.retrieveManagerFromYaml(path);
        } catch (PricingParsingException e) {
            fail(input + e.getMessage());
        }
    }

    @Test
    void givenNoVersionInYamlShouldUpdateToLatestVersion() {
        String path = "parsing/pricing-manager/version/positive/null-version-defaults-to-v1.0.yml";
        try {
            PricingManager pm = YamlUtils.retrieveManagerFromYaml(path);
            assertEquals(Version.V1_1,pm.getVersion());
        } catch (PricingParsingException e) {
            fail(e.getMessage());
        } catch (FilepathException e) {
            fail();
        }
    }

    @Test
    void giveVersionV10ShouldParse() {
        String path = "parsing/pricing-manager/positive/v1.0.yml";

        try  {
            PricingManager pm = YamlUtils.retrieveManagerFromYaml(path);
            assertEquals(Version.V1_1, pm.getVersion());
            assertEquals(LocalDate.of(2024,8,31), pm.getCreatedAt());
            assertNull(pm.getStarts());
            assertNull(pm.getEnds());

        } catch (PricingParsingException e) {
            fail(e.getMessage());
        }

    }

    @Test
    void givenVersionV11ShouldParse() {

        Yaml yaml = new Yaml();
        String path = "parsing/pricing-manager/positive/v1.1.yml";
        try {
            PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(path);
            assertEquals(Version.V1_1, pricingManager.getVersion());
            assertEquals(LocalDate.of(2024, 8, 30), pricingManager.getCreatedAt());
            // 1704110400000 milliseconds => 2024-01-01 12:00:00
            assertEquals(new Date(1704110400000L), pricingManager.getStarts());
            // 1735732800000 milliseconds => 2025-01-01 12:00:00
            assertEquals(new Date(1735732800000L), pricingManager.getEnds());
        } catch (PricingParsingException e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/features-negative.csv", delimiter = ';', useHeadersInDisplayName = true, numLinesToSkip = 1)
    void givenNegativeCasesFeaturesShouldThrow(String fileName, String expectedErrorMessage) {

        String path = String.format("parsing/pricing-manager/features/negative/%s.yml", fileName);

        try {
            YamlUtils.retrieveManagerFromYaml(path);
            fail();
        } catch (FilepathException e ) {
            fail(e.getMessage());
        }  catch (PricingParsingException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/plans-negative.csv", delimiter = ';', useHeadersInDisplayName = true)
    void givenNegativeCasesPlansShouldThrow(String fileName, String expectedErrorMessage) {

        String path = String.format("parsing/pricing-manager/plans/negative/%s.yml", fileName);

        try {
            YamlUtils.retrieveManagerFromYaml(path);
            fail();
        } catch (FilepathException e ) {
            System.out.println(path);
            fail(e.getMessage());
        }  catch (PricingParsingException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/pricing-manager-negative.csv", delimiter = ';', useHeadersInDisplayName = true)
    void givenCSVOfYamlShouldThrowParsingExceptions(String fileName, String expectedErrorMessage) {

        String path = String.format("parsing/pricing-manager/negative/%s.yml", fileName);

        try {
            YamlUtils.retrieveManagerFromYaml(path);
            fail();
        } catch (PricingParsingException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/version-negative.csv", delimiter = ';', useHeadersInDisplayName = true)
    void givenDifferentInvalidVersionsShouldThrow(String fileName, String expectedErrorMessage) {

        String path = String.format("parsing/pricing-manager/version/negative/%s.yml", fileName);

        try {
            YamlUtils.retrieveManagerFromYaml(path);
        } catch (PricingParsingException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/rules-negative.csv", delimiter = ';', useHeadersInDisplayName = true)
    void givenSemanticInvalidPricingShouldThrow(String fileName, String expectedErrorMessage) {

        String path = String.format("parsing/rules/negative/%s.yml", fileName);

        try {
            YamlUtils.retrieveManagerFromYaml(path);
            fail();
        } catch (PricingParsingException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        } catch (InvalidPlanException e) {
            assertEquals(expectedErrorMessage, e.getMessage());

        }
    }

}
