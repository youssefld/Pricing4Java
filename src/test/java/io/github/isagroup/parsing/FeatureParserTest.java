package io.github.isagroup.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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

        try {
            YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-type.yml");
            fail();
        } catch (PricingParsingException e) {
            assertEquals("feature 'type' is mandatory", e.getMessage());
        }

    }

    @Test
    void givenNullValueTypeShouldThrowNullPointerException() {
        try {
            YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "null-value-type.yml");
            fail();
        } catch (PricingParsingException e) {
            assertEquals("Feature value type is null", e.getMessage());
        }

    }

    @Test
    void givenUnsuportedValueTypeShouldThrowIllegalArgumentException() {
        try {
            YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "unsuported-value-type.yml");
            fail();
        } catch (PricingParsingException e) {
            assertEquals("The feature foo does not have a supported valueType. Current valueType: foo", e.getMessage());
        }
    }

    @Test
    void givenFeatureWithNullNameShouldThrowException() {

        try {
            YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "feature-null-as-key.yml");
            fail();
        } catch (PricingParsingException e) {
            assertEquals("A feature cannot have the name null", e.getMessage());
        }
    }

    @Test
    void givenKeyValueInFeaturesShouldThrowClassCastException() {

        try {
            YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "features-is-key-value.yml");
            fail();
        } catch (PricingParsingException e) {
            assertEquals("The feature foo is not defined correctly. All its options must be specified, and it cannot be defined as a key-value pair", e.getMessage());
        }
    }
}
