package io.github.isagroup.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.yaml.YamlUtils;

public class FeatureParserTest {

    private static final String TEST_CASES = "parsing/features/";
    private static final String POSITIVE_CASES = TEST_CASES + "positive/";
    private static final String NEGATIVE_CASES = TEST_CASES + "negative/";

    @Test
    void givenPaymentFeatureBasicPlanValueShouldBeAList() {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(POSITIVE_CASES + "payment-feature.yml");

        List<String> overwrittenPaymentMethods = (List<String>) pricingManager.getPlans().get("BASIC").getFeatures()
                .get("payment")
                .getValue();

        assertInstanceOf(List.class, overwrittenPaymentMethods,
                "Payment methods is not a list of payment methods");
        assertEquals(2, overwrittenPaymentMethods.size());

    }

    @Test
    void givenPaymentFeatureDefaultValueShouldBeListOfPaymentMethods() {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(POSITIVE_CASES + "payment-feature.yml");

        List<String> expectedPaymentMethods = new ArrayList<>();
        expectedPaymentMethods.add("CARD");
        List<String> actualPaymentMethods = (List<String>) pricingManager.getFeatures().get("payment")
                .getDefaultValue();

        assertEquals(expectedPaymentMethods, actualPaymentMethods,
                "Payment methods should be a list of payment methods");

    }

    @Test
    void givenNonNullFeaturesInBasicPlanShouldHaveDefaultValues() {

        PricingManager pricingManager = YamlUtils
                .retrieveManagerFromYaml(POSITIVE_CASES + "non-null-features-basic.yml");

        Boolean value = (Boolean) pricingManager.getPlans().get("BASIC").getFeatures().get("featureA")
                .getValue();

        assertEquals(true, value);
    }

    @Test
    void givenNullFeaturesBasicPlanShouldHaveDefaultValues() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(POSITIVE_CASES + "null-features-basic.yml");

        Boolean value = (Boolean) pricingManager.getPlans().get("BASIC").getFeatures().get("featureA")
                .getValue();

        assertNull(value, "BASIC plan value is not null");
    }

    @Test
    void givenNullFeaturesShouldParseAListOfDefaultFeatures() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(POSITIVE_CASES + "null-features-basic.yml");

        boolean value = pricingManager.getPlans().get("BASIC").getFeatures().isEmpty();

        assertEquals(false, value, "Basic plan does not have a list of features");
    }

    @Test
    void givenNullTypeShouldThrowNullPointerException() {

        assertThrows(NullPointerException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-type.yml"));
    }

    @Test
    void givenNullValueTypeShouldThrowNullPointerException() {

        assertThrows(NullPointerException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-value-type.yml"));
    }

    @Test
    void givenUnsuportedValueTypeShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "unsuported-value-type.yml"));
    }

    @Test
    void givenFeatureWithNullNameShouldThrowException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "feature-null-as-key.yml"));
    }

    @Test
    void givenKeyValueInFeaturesShouldThrowClassCastException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "features-is-key-value.yml"));
    }
}
