package io.github.isagroup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.isagroup.exceptions.InvalidDefaultValueException;
import io.github.isagroup.exceptions.InvalidValueTypeException;
import io.github.isagroup.models.Feature;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.yaml.YamlUtils;
import io.github.isagroup.utils.PricingValidators;

public class PricingValidationTests {

    @Test
    @Disabled
    void givenNullFeatureShouldThrowIllegalArgumentException() {

        Feature feature = null;
        assertThrows(IllegalArgumentException.class, () -> PricingValidators.validateAndFormatFeature(feature));
    }

    @Test
    void givenNullExpressionShouldThrowIllegalArgumentException() {

        String expression = null;
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/petclinic.yml");
        Feature feature = pricingManager.getFeatures().get("maxPets");
        feature.setExpression(expression);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> PricingValidators.validateAndFormatFeature(feature));

        assertEquals(
                "The feature " + feature.getName()
                        + " expression must have at most 1000 characters and must be a string",
                ex.getMessage());

    }

    @Test
    void given10001CharactersExpressionShouldThrowIllegalArgumentException() {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1001; i++) {
            sb.append("a");
        }
        String expression = sb.toString();
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/petclinic.yml");
        Feature feature = pricingManager.getFeatures().get("maxPets");
        feature.setExpression(expression);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> PricingValidators.validateAndFormatFeature(feature));
        assertEquals(
                "The feature " + feature.getName()
                        + " expression must have at most 1000 characters and must be a string",
                ex.getMessage());

    }

    @Test
    void givenNullValueTypeShouldThrowInvalidValueTypeException() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/petclinic.yml");
        Feature feature = pricingManager.getFeatures().get("maxPets");
        feature.setValueType(null);

        // FIXME: REMOVE VALUE TYPE CHECK
        InvalidValueTypeException ex = assertThrows(InvalidValueTypeException.class,
                () -> PricingValidators.validateAndFormatFeature(feature));
        assertEquals(
                "The feature " + feature.getName() + " valueType must not be null",
                ex.getMessage());

    }

    @Test
    void givenNullDefaultValueShouldThrowWhenValueTypeIsNumeric() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/petclinic.yml");
        Feature feature = pricingManager.getFeatures().get("maxPets");
        feature.setDefaultValue(null);

        InvalidDefaultValueException ex = assertThrows(InvalidDefaultValueException.class,
                () -> PricingValidators.validateAndFormatFeature(feature));
        assertEquals(
                "The feature " + feature.getName()
                        + " defaultValue must not be null",
                ex.getMessage());

    }

    @Test
    void givenStringDefaultValueShouldThrowWhenValueTypeIsNumeric() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/petclinic.yml");
        Feature feature = pricingManager.getFeatures().get("maxPets");
        String defaultValue = "";
        feature.setDefaultValue(defaultValue);

        // FIXME: REMOVE MULTIPLE VALUE TYPE CHECK
        InvalidDefaultValueException ex = assertThrows(InvalidDefaultValueException.class,
                () -> PricingValidators.validateAndFormatFeature(feature));
        assertEquals(
                "The feature " + feature.getName()
                        + " defaultValue must be one of the supported numeric types if valueType is NUMERIC",
                ex.getMessage());

    }

    @Test
    void givenStringDefaultValueShouldThrowWhenValueTypeIsBoolean() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/petclinic.yml");
        Feature feature = pricingManager.getFeatures().get("haveCalendar");
        String defaultValue = "";
        feature.setDefaultValue(defaultValue);

        InvalidDefaultValueException ex = assertThrows(InvalidDefaultValueException.class,
                () -> PricingValidators.validateAndFormatFeature(feature));
        assertEquals(
                "The feature " + feature.getName()
                        + " defaultValue must be a boolean if valueType is BOOLEAN",
                ex.getMessage());

    }

    @Test
    void givenBooleanDefaultValueShouldThrowWhenValueTypeIsText() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/petclinic.yml");
        Feature feature = pricingManager.getFeatures().get("supportPriority");
        Boolean defaultValue = true;
        feature.setDefaultValue(defaultValue);

        InvalidDefaultValueException ex = assertThrows(InvalidDefaultValueException.class,
                () -> PricingValidators.validateAndFormatFeature(feature));
        assertEquals(
                "The feature " + feature.getName()
                        + " defaultValue must be a string if valueType is TEXT",
                ex.getMessage());

    }

    // Remove logic from validateValueTypeConsistency

    @Test
    void givenNumericExpressionShouldThrowIllegalArgumentExceptionWhenIsBoolean() {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/petclinic.yml");
        Feature feature = pricingManager.getFeatures().get("haveCalendar");
        String expression = "a < b";
        feature.setExpression(expression);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> PricingValidators.validateAndFormatFeature(feature));
        assertEquals(
                "Expression of feature " + feature.getName()
                        + " should only include the feature value/defaultValue and the operators '&&', '||' and '!', as it is BOOLEAN",
                ex.getMessage());
    }
}
