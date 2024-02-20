package io.github.isagroup.parsing;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.services.yaml.YamlUtils;

public class PricingManagerParserTest {

    private static final String NEGATIVE_CASES = "parsing/negative/";

    @Test
    void given_null_saasname_should_throw_parsing_exception() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-saasname.yml"));
    }

    @Test
    void given_null_day_should_throw_parsing_exception() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-day.yml"));
    }

    @Test
    void given_null_month_should_throw_parsing_exception() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-month.yml"));
    }

    @Test
    void given_null_year_should_throw_parsing_exception() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-year.yml"));
    }

    @Test
    void given_null_currency_should_throw_parsing_exception() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-currency.yml"));
    }

    @Test
    void given_null_features_should_throw_parsing_exception() {
        assertThrows(IllegalArgumentException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-features.yml"));
    }

    @Test
    void given_string_in_features_hould_throw_ClassCastException() {
        assertThrows(ClassCastException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "features-is-string.yml"));
    }

    @Test
    void given_list_in_features_should_throw_ClassCastException() {
        assertThrows(ClassCastException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "features-is-list.yml"));
    }

    @Test
    void given_key_value_in_features_should_throw_ClassCastException() {
        assertThrows(ClassCastException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "features-is-key-value.yml"));
    }

    @Test
    void given_null_plans_should_pass() {
        YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-plans.yml");
    }

}