package io.github.isagroup.parsing;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.isagroup.services.yaml.YamlUtils;

public class FeatureParserTest {

    private static final String TEST_CASES = "parsing/features/";

    @Test
    void givenNullTypeShouldThrowNullPointerException() {

        assertThrows(NullPointerException.class,
                () -> YamlUtils.retrieveManagerFromYaml(TEST_CASES + "null-type.yml"));

    }

    @Test
    void givenNullValueTypeShouldThrowNullPointerException() {

        assertThrows(NullPointerException.class,
                () -> YamlUtils.retrieveManagerFromYaml(TEST_CASES + "null-value-type.yml"));
    }

    @Test
    void givenUnsuportedValueTypeShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> YamlUtils.retrieveManagerFromYaml(TEST_CASES + "unsuported-value-type.yml"));
    }

}
