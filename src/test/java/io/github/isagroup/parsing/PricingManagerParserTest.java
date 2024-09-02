package io.github.isagroup.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.Version;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.parsing.PricingManagerParser;
import io.github.isagroup.services.yaml.YamlUtils;

public class PricingManagerParserTest {

    @Test
    void givenPetclinicShouldGetPricingManager() {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml("pricing/petclinic.yml");

        assertTrue(pricingManager.getPlans().get("BASIC") instanceof Plan,
                "Should be an instance of PricingManager");
        assertEquals(false,
                pricingManager.getPlans().get("BASIC").getFeatures().get("haveCalendar")
                        .getDefaultValue(),
                "The deafult value of the haveCalendar feature should be false");
        assertEquals(null, pricingManager.getPlans().get("BASIC").getFeatures().get("maxPets").getValue(),
                "The value of the maxPets should be null");

    }

    @Test
    void givenNoVersionShouldDefaultToVersionOneDotZero() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: Pricing Without Version Should Default to 1.0
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans:
                  BASIC:
                    description: Basic plan
                    monthlyPrice: 0.0
                    annualPrice: 0.0
                    unit: user/month
                    features: null
                    usageLimits: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        PricingManager pricingManager = PricingManagerParser.parseMapToPricingManager(configFile);

        assertEquals(pricingManager.getVersion(), new Version(1, 0));

    }

    @Test
    void givenOneDotZeroVersionInFileShouldBeOk() {

        Yaml yaml = new Yaml();

        String file = """
                version: "1.0"
                saasName: Pricing Without Version Should Default to 1.0
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans:
                  BASIC:
                    description: Basic plan
                    monthlyPrice: 0.0
                    annualPrice: 0.0
                    unit: user/month
                    features: null
                    usageLimits: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        PricingManager pricingManager = PricingManagerParser.parseMapToPricingManager(configFile);

        assertEquals(pricingManager.getVersion(), new Version(1, 0));

    }

    @Test
    void givenPricingVersionOneDotZeroAsFloatShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                version: 1.0
                saasName: Pricing Without Version Should Default to 1.0
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans:
                  BASIC:
                    description: Basic plan
                    monthlyPrice: 0.0
                    annualPrice: 0.0
                    unit: user/month
                    features: null
                    usageLimits: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenInvalidPricingVersionShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                version: Invalid version
                saasName: Pricing Without Version Should Default to 1.0
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans:
                  BASIC:
                    description: Basic plan
                    monthlyPrice: 0.0
                    annualPrice: 0.0
                    unit: user/month
                    features: null
                    usageLimits: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));
    }

    @Test
    void givenOneDotOneAndDayMonthYearSyntaxShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                version: "1.1"
                saasName: Version and old syntax missmatch
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                createdAt: "2024-08-30"
                starts: 2024-01-01 12:00:00
                ends: 2025-01-01 12:00:00
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans:
                  BASIC:
                    description: Basic plan
                    monthlyPrice: 0.0
                    annualPrice: 0.0
                    unit: user/month
                    features: null
                    usageLimits: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));
    }

    @Test
    void givenOneDotOneShouldDectectNewSyntax() {

        Yaml yaml = new Yaml();

        String file = """
                version: "1.1"
                saasName: Version and old syntax missmatch
                currency: EUR
                hasAnnualPayment: false
                createdAt: "2024-08-30"
                starts: 2024-01-01 12:00:00
                ends: 2025-01-01 12:00:00
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans:
                  BASIC:
                    description: Basic plan
                    monthlyPrice: 0.0
                    annualPrice: 0.0
                    unit: user/month
                    features: null
                    usageLimits: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        PricingManager pricingManager = PricingManagerParser.parseMapToPricingManager(configFile);

        assertEquals(pricingManager.getCreatedAt(), LocalDate.of(2024, 8, 30));
        // 1704110400000 milliseconds => 2024-01-01 12:00:00
        assertEquals(pricingManager.getStarts(), new Date(1704110400000L));
        // 1735732800000 milliseconds => 2025-01-01 12:00:00
        assertEquals(pricingManager.getEnds(), new Date(1735732800000L));
    }

    @Test
    void givenVersionOneDotOneNonExistentCreatedAtShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                version: "1.1"
                saasName: Version and old syntax missmatch
                currency: EUR
                hasAnnualPayment: false
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans:
                  BASIC:
                    description: Basic plan
                    monthlyPrice: 0.0
                    annualPrice: 0.0
                    unit: user/month
                    features: null
                    usageLimits: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenBooleanInCreatedAtInOneDotOneVersionShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                version: "1.1"
                saasName: Version and old syntax missmatch
                currency: EUR
                hasAnnualPayment: false
                createdAt: true
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans:
                  BASIC:
                    description: Basic plan
                    monthlyPrice: 0.0
                    annualPrice: 0.0
                    unit: user/month
                    features: null
                    usageLimits: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenInvalidDateFormatInFieldCreatedAtVersionOneDotShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                version: "1.1"
                saasName: Version and old syntax missmatch
                currency: EUR
                hasAnnualPayment: false
                createdAt: Invalid date format :(
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans:
                  BASIC:
                    description: Basic plan
                    monthlyPrice: 0.0
                    annualPrice: 0.0
                    unit: user/month
                    features: null
                    usageLimits: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenInvalidTimestampInStartsFieldAtVersionOneDotShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                version: "1.1"
                saasName: Version and old syntax missmatch
                currency: EUR
                hasAnnualPayment: false
                createdAt: "2024-08-31"
                starts: Invalid timestamp
                ends: 2025-01-01 12:00:00
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans:
                  BASIC:
                    description: Basic plan
                    monthlyPrice: 0.0
                    annualPrice: 0.0
                    unit: user/month
                    features: null
                    usageLimits: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenInvalidTimestampInEndsFieldAtVersionOneDotShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                version: "1.1"
                saasName: Version and old syntax missmatch
                currency: EUR
                hasAnnualPayment: false
                createdAt: "2024-08-31"
                starts: 2024-01-01 12:00:00
                ends: Invalid timestamp
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans:
                  BASIC:
                    description: Basic plan
                    monthlyPrice: 0.0
                    annualPrice: 0.0
                    unit: user/month
                    features: null
                    usageLimits: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenNullInSaasNameShouldThrow() {

        Yaml yaml = new Yaml();
        String file = "saasName: null";
        Map<String, Object> configFile = yaml.load(file);
        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenOtherThanStringInSaasNameShouldThrow() {

        Yaml yaml = new Yaml();
        String file = "saasName: true";
        Map<String, Object> configFile = yaml.load(file);
        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenNullInDayShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: checking-day-field
                currency: EUR
                hasAnnualPayment: false
                day: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenOtherThanIntegerInDayShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: checking-day-field
                currency: EUR
                hasAnnualPayment: false
                day: false
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenNullInMonthShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: checking-month
                currency: EUR
                hasAnnualPayment: false
                day: 1
                month: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenOtherThanIntegerInMonthShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: checking-month
                currency: EUR
                hasAnnualPayment: false
                day: 1
                month: true
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenNullInYearShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: checking-month
                currency: EUR
                hasAnnualPayment: false
                day: 1
                month: 1
                year: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenOtherThanIntegerInYearShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: checking-month
                currency: EUR
                hasAnnualPayment: false
                day: 1
                month: 1
                year: true
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));
    }

    @Test
    void givenMalformedDateShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: checking-month
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 13
                year: 2024
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenNullInCurrencyShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: checking-month
                currency: null
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenOtherThanStringShouldThrow() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: checking-month
                currency: true
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class, () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenNullFeaturesShouldThrowParsingException() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: Null features
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));
    }

    @Test
    void givenStringInFeaturesShouldThrowException() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: features is string
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features: Invalid string
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));
    }

    @Test
    void givenIntegerInFeaturesShouldThrowException() {
        Yaml yaml = new Yaml();

        String file = """
                saasName: features is string
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features: 1
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));
    }

    @Test
    void givenListInFeaturesShouldThrowException() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: features is a list
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features:
                  - foo
                  - bar
                  - baz
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenFloatInFeaturesShouldThrowException() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: features is a float
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features: 1.0
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));
    }

    @Test
    void givenBooleanInFeaturesShouldThrowException() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: features is a list
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features: true
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));
    }

    @Test
    void givenStringPlansShouldThrowException() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: plans is a string
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans: Invalid string
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));

    }

    @Test
    void givenIntegerPlansShouldThrowException() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: plans is a integer
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans: 1
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));
    }

    @Test
    void givenFloatPlansShouldThrowException() {
        Yaml yaml = new Yaml();

        String file = """
                saasName: plans is a string
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans: 1.0
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));
    }

    @Test
    void givenBooleanPlansShouldThrowException() {
        Yaml yaml = new Yaml();

        String file = """
                saasName: plans is a string
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans: true
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));
    }

    @Test
    void givenListPlansShouldThrowException() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: plans is a string
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans:
                  - foo
                  - bar
                  - baz
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));
    }

    @Test
    void givenNullPlansAndAddOnsShouldThrowException() {

        Yaml yaml = new Yaml();

        String file = """
                saasName: plans is a string
                currency: EUR
                hasAnnualPayment: false
                day: 31
                month: 8
                year: 2024
                features:
                  foo:
                    type: DOMAIN
                    valueType: TEXT
                    defaultValue: baz
                plans: null
                addOns: null
                """;

        Map<String, Object> configFile = yaml.load(file);

        assertThrows(PricingParsingException.class,
                () -> PricingManagerParser.parseMapToPricingManager(configFile));
    }

}
