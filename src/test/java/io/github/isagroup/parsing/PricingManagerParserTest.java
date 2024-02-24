package io.github.isagroup.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
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

    @Test
    void givenPetclinicShouldGetPricingManager() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/petclinic.yml");

        assertTrue(pricingManager.getPlans().get("BASIC") instanceof Plan, "Should be an instance of PricingManager");
        assertEquals(false, pricingManager.getPlans().get("BASIC").getFeatures().get("haveCalendar").getDefaultValue(),
                "The deafult value of the haveCalendar feature should be false");
        assertEquals(null, pricingManager.getPlans().get("BASIC").getFeatures().get("maxPets").getValue(),
                "The value of the maxPets should be null");

    }

    @Test
    void givenPaymentFeatureDefaultValueShouldBeListOfPaymentMethods() {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("parsing/payment-feature.yml");

        List<String> expectedPaymentMethods = new ArrayList<>();
        expectedPaymentMethods.add("CARD");
        List<String> actualPaymentMethods = (List<String>) pricingManager.getFeatures().get("payment")
                .getDefaultValue();

        assertEquals(expectedPaymentMethods, actualPaymentMethods,
                "Payment methods should be a list of payment methods");

    }

    @Test
    void givenPaymentFeatureBasicPlanValueShouldBeAList() {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("parsing/payment-feature.yml");

        List<String> overwrittenPaymentMethods = (List<String>) pricingManager.getPlans().get("BASIC").getFeatures()
                .get("payment")
                .getValue();

        assertInstanceOf(List.class, overwrittenPaymentMethods,
                "Payment methods is not a list of payment methods");
        assertEquals(2, overwrittenPaymentMethods.size());

    }

    @Test
    void givenNonNullFeaturesInBasicPlanShouldHaveDefaultValues() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("parsing/non-null-features-basic.yml");

        Boolean value = (Boolean) pricingManager.getPlans().get("BASIC").getFeatures().get("featureA")
                .getValue();

        assertEquals(true, value);
    }

    @Test
    void givenNullFeaturesInBasicPlanShouldHaveDefaultValues() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("parsing/null-features-basic.yml");

        Boolean value = (Boolean) pricingManager.getPlans().get("BASIC").getFeatures().get("featureA")
                .getValue();

        assertNull(value, "BASIC plan value is not null");
    }

    @Test
    void givenNullFeaturesShouldParseAListOfDefaultFeatures() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("parsing/null-features-basic.yml");

        boolean value = pricingManager.getPlans().get("BASIC").getFeatures().isEmpty();

        assertEquals(false, value, "Basic plan does not have a list of features");
    }

}