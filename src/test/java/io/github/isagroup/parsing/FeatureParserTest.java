package io.github.isagroup.parsing;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.isagroup.services.yaml.YamlUtils;

public class FeatureParserTest {

    private static final String TEST_CASES = "parsing/features/";

    @Test
    void given_null_type_should_throw_NullPointerException() {

        assertThrows(NullPointerException.class,
                () -> YamlUtils.retrieveManagerFromYaml(TEST_CASES + "null-type.yml"));

    }

    @Test
    void given_null_value_type_should_throw_NullPointerException() {

        assertThrows(NullPointerException.class,
                () -> YamlUtils.retrieveManagerFromYaml(TEST_CASES + "null-value-type.yml"));
    }

    @Test
    void given_unsuported_value_type_should_throw_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> YamlUtils.retrieveManagerFromYaml(TEST_CASES + "unsuported-value-type.yml"));
    }

}
