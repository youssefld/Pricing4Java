package io.github.isagroup.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.yaml.snakeyaml.Yaml;

import io.github.isagroup.exceptions.InvalidPlanException;
import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.parsing.PricingManagerParser;
import io.github.isagroup.services.yaml.YamlUtils;

public class AddOnParserTest {

    private final static String ADD_ONS_TEST_CASES_PATH = "parsing/add-ons/";
    private final static String NEGATIVE_CASES = ADD_ONS_TEST_CASES_PATH + "negative/";

    @Test
    void givenInvalidSyntaxAddOnUsageLimitShouldThrowException() {

        Exception ex = assertThrows(PricingParsingException.class,
                () -> YamlUtils
                        .retrieveManagerFromYaml(NEGATIVE_CASES + "add-ons-usage-limits-not-a-map.yml"));
        assertEquals("The usage limit fooLimit of the add-on Baz is not a valid map", ex.getMessage());
    }

    @Test
    void givenInvalidSyntaxAddOnUsageLimitExtensionsShouldThrowException() {

        Exception ex = assertThrows(PricingParsingException.class,
                () -> YamlUtils
                        .retrieveManagerFromYaml(NEGATIVE_CASES + "add-ons-usage-limits-extensions-not-a-map.yml"));
        assertEquals("The usage limit fooLimit of the add-on Baz is not a valid map", ex.getMessage());
    }

    @Test
    void givenAddOnThatDependsOnAnotherAddonCreatesPricingManager() {

        Yaml yaml = new Yaml();
        String path = "src/test/resources/parsing/add-on-depending-on-another-add-on.yml";
        try {
            Map<String, Object> configFile = yaml
                    .load(new FileInputStream(path));
            PricingManager pricingManager = PricingManagerParser.parseMapToPricingManager(configFile);
            assertEquals(2, pricingManager.getAddOns().size());
        } catch (FileNotFoundException e) {
            fail(String.format("The file with location '%s' was not found", path));
        }
    }

    @Test
    void givenUndefinedAddOnInASpecificAddOnShouldThrow() {

        Yaml yaml = new Yaml();
        String path = "src/test/resources/parsing/add-on-depending-on-unexistent-add-on.yml";
        try {
            Map<String, Object> configFile = yaml
                    .load(new FileInputStream(path));
            PricingManagerParser.parseMapToPricingManager(configFile);
            fail();
        } catch (FileNotFoundException e) {
            fail(String.format("The file with location '%s' was not found", path));
        } catch (InvalidPlanException e) {
            assertEquals("The plan or addOn bax is not defined in the pricing manager", e.getMessage());
        }
    }

}
