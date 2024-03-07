package io.github.isagroup.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.yaml.YamlUtils;

public class PricingManagerParserTest {

    private static final String PRICING_MANAGER_TEST_CASES = "parsing/pricing-manager";
    private static final String NEGATIVE_CASES = PRICING_MANAGER_TEST_CASES + "/negative/";

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
    void givenNullSaasNameShouldThrowParsingException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "01-null-saasname.yml"));
    }

    @Test
    void givenNullDayShouldThrowParsingException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "02-null-day.yml"));
    }

    @Test
    void givenNullMonthShouldThrowParsingException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "03-null-month.yml"));
    }

    @Test
    void givenNullYearShouldThrowParsingException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "04-null-year.yml"));
    }

    @Test
    void givenNullCurrencyShouldThrowParsingException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "05-null-currency.yml"));
    }

    @Test
    void givenNullFeaturesShouldThrowParsingException() {
        assertThrows(IllegalArgumentException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "06-0-null-features.yml"));
    }

    @Test
    void givenStringInFeaturesShouldThrowException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "06-1-features-is-string.yml"));
    }

    @Test
    void givenListInFeaturesShouldThrowException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "06-2-features-is-list.yml"));
    }

    @Test
    void givenIntegerInFeaturesShouldThrowException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "06-3-features-is-integer.yml"));
    }

    @Test
    void givenFloatInFeaturesShouldThrowException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "06-4-features-is-float.yml"));
    }

    @Test
    void givenBooleanInFeaturesShouldThrowException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "06-5-features-is-boolean.yml"));
    }

    // @Test
    // void givenNullPlansShouldThrowException() {
    //     assertThrows(PricingParsingException.class,
    //             () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "07-0-null-plans.yml"));
    // }

    @Test
    void givenStringPlansShouldThrowException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "07-1-plans-is-string.yml"));
    }

    @Test
    void givenListPlansShouldThrowException() {
        
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "07-2-plans-is-list.yml"));
    }

    @Test
    void givenIntegerPlansShouldThrowException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "07-3-plans-is-integer.yml"));
    }

    @Test
    void givenFloatPlansShouldThrowException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "07-4-plans-is-float.yml"));
    }

    @Test
    void givenBooleanPlansShouldThrowException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "07-5-plans-is-boolean.yml"));
    }

    @Test
    void givenNullPlansAndAddOnsShouldThrowException() {
        assertThrows(PricingParsingException.class,
                () -> YamlUtils.retrieveManagerFromYaml(NEGATIVE_CASES + "08-null-plans-and-addons.yml"));
    }

}