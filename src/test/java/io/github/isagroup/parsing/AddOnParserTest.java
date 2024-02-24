package io.github.isagroup.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.services.yaml.YamlUtils;

public class AddOnParserTest {

    private final static String ADD_ONS_TEST_CASES_PATH = "parsing/add-ons/";
    private final static String NEGATIVE_CASES = ADD_ONS_TEST_CASES_PATH + "negative/";

    @Test
    @Disabled
    void givenInvalidSyntaxAddOnUsageLimitShouldThrowException() {

        Exception ex = assertThrows(PricingParsingException.class,
                () -> YamlUtils
                        .retrieveManagerFromYaml(NEGATIVE_CASES + "add-ons-usage-limits-not-a-map.yml"));
        assertEquals("CHANGE ME", ex.getMessage());
    }

    @Test
    @Disabled
    void givenInvalidSyntaxAddOnUsageLimitExtensionsShouldThrowException() {

        Exception ex = assertThrows(PricingParsingException.class,
                () -> YamlUtils
                        .retrieveManagerFromYaml(NEGATIVE_CASES + "add-ons-usage-limits-extensions-not-a-map.yml"));
        assertEquals("CHANGE ME", ex.getMessage());
    }

}
