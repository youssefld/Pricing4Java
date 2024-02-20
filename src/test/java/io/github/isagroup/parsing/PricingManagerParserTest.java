package io.github.isagroup.parsing;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.services.yaml.YamlUtils;

public class PricingManagerParserTest {

    private static final String NEGATIVE_CASES = "parsing/negative/";

    @Test
    void givenNullSaasNameShouldThrowParsingException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-saasname.yml"));
    }

    @Test
    void givenNullDayShouldThrowParsingException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-day.yml"));
    }

    @Test
    void givenNullMonthShouldThrowParsingException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-month.yml"));
    }

    @Test
    void givenNullYearShouldThrowParsingException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-year.yml"));
    }

    @Test
    void givenNullCurrencyShouldThrowParsingException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-currency.yml"));
    }

    @Test
    void givenNullFeaturesShouldThrowParsingException() {
        assertThrows(IllegalArgumentException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-features.yml"));
    }

    @Test
    void givenStringInFeaturesShouldThrowClassCastException() {
        assertThrows(ClassCastException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "features-is-string.yml"));
    }

    @Test
    void givenListInFeaturesShouldThrowClassCastException() {
        assertThrows(ClassCastException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "features-is-list.yml"));
    }

    @Test
    void givenKeyValueInFeaturesShouldThrowClassCastException() {
        assertThrows(ClassCastException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "features-is-key-value.yml"));
    }

    @Test
    void givenNullPlansShouldPass() {
        YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-plans.yml");
    }

}