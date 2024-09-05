package io.github.isagroup.services.parsing;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.AddOn;
import io.github.isagroup.models.Feature;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.models.UsageLimit;
import io.github.isagroup.models.Version;

public class PricingManagerParser {

    private PricingManagerParser() {
    }

    public static PricingManager parseMapToPricingManager(Map<String, Object> yamlConfigMap) {

        PricingManager pricingManager = new PricingManager();

        setBasicAttributes(yamlConfigMap, pricingManager);
        setFeatures(yamlConfigMap, pricingManager);
        setUsageLimits(yamlConfigMap, pricingManager);
        setPlans(yamlConfigMap, pricingManager);
        setAddOns(yamlConfigMap, pricingManager);

        if (pricingManager.getPlans() == null && pricingManager.getAddOns() == null) {
            throw new PricingParsingException("The pricing manager does not have any plans or add ons");
        }

        return pricingManager;
    }

    private static void setBasicAttributes(Map<String, Object> yamlConfigMap, PricingManager pricingManager)
            throws PricingParsingException {

        if (yamlConfigMap.get("version") == null) {
            pricingManager.setVersion(new Version(1, 0));
        } else if (yamlConfigMap.get("version") != null && !(yamlConfigMap.get("version") instanceof String)) {
            throw new PricingParsingException(
                    "'version' is not a string, check the specification." +
                            "Version has to be formmated like <major.minor>.");
        } else {
            String versionToCheck = (String) yamlConfigMap.get("version");
            Optional<Version> version = Version.valueOf(versionToCheck);
            if (version.isEmpty()) {
                throw new PricingParsingException(String.format("version '%s' is invalid", versionToCheck));
            }

            pricingManager.setVersion(version.get());
        }

        if (yamlConfigMap.get("saasName") == null) {
            throw new PricingParsingException("SaasName was not defined");
        }

        if (!(yamlConfigMap.get("saasName") instanceof String)) {
            throw new PricingParsingException("'saasName' has to be a string");
        }

        pricingManager.setSaasName((String) yamlConfigMap.get("saasName"));

        if (yamlConfigMap.get("currency") == null) {
            throw new PricingParsingException("Currency was not defined");
        }

        if (!(yamlConfigMap.get("currency") instanceof String)) {
            throw new PricingParsingException("'currency' has to be a string");
        }

        pricingManager.setCurrency((String) yamlConfigMap.get("currency"));

        if (yamlConfigMap.get("hasAnnualPayment") != null) {
            pricingManager.setHasAnnualPayment((Boolean) yamlConfigMap.get("hasAnnualPayment"));
        }

        if (pricingManager.getVersion().equals(new Version(1, 0))) {
            if (yamlConfigMap.get("day") == null) {
                throw new PricingParsingException("Day of plan was not defined");
            }

            if (!(yamlConfigMap.get("day") instanceof Integer)) {
                throw new PricingParsingException("'day' is expected to be an integer");
            }

            if (yamlConfigMap.get("month") == null) {
                throw new PricingParsingException("Month of plan was not defined");
            }

            if (!(yamlConfigMap.get("month") instanceof Integer)) {
                throw new PricingParsingException("'month' is expected to be an integer");
            }

            if (yamlConfigMap.get("year") == null) {
                throw new PricingParsingException("Year of plan was not defined");
            }

            if (!(yamlConfigMap.get("year") instanceof Integer)) {
                throw new PricingParsingException("'year' is expected to be an integer");
            }

            int day = (int) yamlConfigMap.get("day");
            int month = (int) yamlConfigMap.get("month");
            int year = (int) yamlConfigMap.get("year");

            try {
                pricingManager.setCreatedAt(LocalDate.of(year, month, day));

            } catch (DateTimeException err) {
                throw new PricingParsingException(String.format(
                        "Cannot convert %d-%d-%d to a LocalDate. Check that day, month and year are valid.", year,
                        month, day));
            }

        } else if (pricingManager.getVersion().equals(new Version(1, 1))) {

            if (yamlConfigMap.get("day") != null
                    || yamlConfigMap.get("month") != null
                    || yamlConfigMap.get("year") != null) {
                throw new PricingParsingException(
                        "You have specified version 1.1 of the config but old configuration fields were encountered from version 1.0 (day, month, year). Please use createdAt and remove day, month and year or remove the version field.");
            }

            if (yamlConfigMap.get("createdAt") == null) {
                throw new PricingParsingException("'createdAt' is mandatory. Check your config file.");
            }

            if (yamlConfigMap.get("createdAt") instanceof Date) {

            } else if (yamlConfigMap.get("createdAt") instanceof String) {
                try {
                    pricingManager.setCreatedAt(LocalDate.parse((String) yamlConfigMap.get("createdAt")));

                } catch (DateTimeParseException err) {
                    throw new PricingParsingException(
                            String.format("date %s is invalid. Use the following format to specify a date yyyy-MM-dd.",
                                    yamlConfigMap.get("createdAt")));
                }

            } else {
                throw new PricingParsingException("createdAt is not a string or a date, change that field.");
            }

            if (yamlConfigMap.get("starts") != null && !(yamlConfigMap.get("starts") instanceof Date)) {
                throw new PricingParsingException("starts is expected to be a timestamp. Check your config file.");
            }

            if (yamlConfigMap.get("ends") != null && !(yamlConfigMap.get("ends") instanceof Date)) {
                throw new PricingParsingException("ends is expected to be a timestamp. Check your config file.");
            }

            pricingManager.setStarts((Date) yamlConfigMap.get("starts"));
            pricingManager.setEnds((Date) yamlConfigMap.get("ends"));

        }

    }

    private static void setFeatures(Map<String, Object> map, PricingManager pricingManager) {
        Map<String, Feature> pricingFeatures = new LinkedHashMap<>();

        Map<String, Object> featuresMap = new HashMap<>();

        if (map.get("features") == null) {
            throw new PricingParsingException(
                    "'features' is mandatory. It should be a map of features with their correspoding attributes.");
        }

        try {
            featuresMap = (Map<String, Object>) map.get("features");
        } catch (ClassCastException e) {
            throw new PricingParsingException(
                    "The features are not defined correctly. It should be a map of features and their options.");
        }

        for (String featureName : featuresMap.keySet()) {

            try {
                Map<String, Object> featureMap = (Map<String, Object>) featuresMap.get(featureName);
                Feature feature = FeatureParser.parseMapToFeature(featureName, featureMap);
                pricingFeatures.put(featureName, feature);
            } catch (ClassCastException e) {
                throw new PricingParsingException("The feature " + featureName
                        + " is not defined correctly. All its options must be specified, and it cannot be defined as a key-value pair");
            }
        }

        pricingManager.setFeatures(pricingFeatures);
    }

    private static void setUsageLimits(Map<String, Object> map, PricingManager pricingManager) {
        Map<String, Object> usageLimitsMap = (Map<String, Object>) map.get("usageLimits");
        Map<String, UsageLimit> usageLimits = new LinkedHashMap<>();

        if (usageLimitsMap == null) {
            return;
        }

        for (String limitName : usageLimitsMap.keySet()) {
            Map<String, Object> limitMap = (Map<String, Object>) usageLimitsMap.get(limitName);
            UsageLimit limit = UsageLimitParser.parseMapToFeature(limitName, limitMap, pricingManager);

            usageLimits.put(limitName, limit);
        }

        pricingManager.setUsageLimits(usageLimits);
    }

    private static void setPlans(Map<String, Object> map, PricingManager pricingManager) {

        Map<String, Object> plansMap = new HashMap<>();

        try {
            plansMap = (Map<String, Object>) map.get("plans");
        } catch (ClassCastException e) {
            throw new PricingParsingException(
                    "The plans are not defined correctly. It should be a map of plans and their options");
        }

        Map<String, Plan> plans = new LinkedHashMap<>();

        if (plansMap == null) {
            return;
        }

        for (String planName : plansMap.keySet()) {

            Map<String, Object> planMap = (Map<String, Object>) plansMap.get(planName);
            Plan plan = PlanParser.parseMapToPlan(planName, planMap, pricingManager);

            plans.put(planName, plan);
        }

        pricingManager.setPlans(plans);
    }

    private static void setAddOns(Map<String, Object> map, PricingManager pricingManager) {
        Map<String, Object> addOnsMap = (Map<String, Object>) map.get("addOns");
        Map<String, AddOn> addOns = new LinkedHashMap<>();

        if (addOnsMap == null) {
            return;
        }

        for (String addOnName : addOnsMap.keySet()) {
            Map<String, Object> addOnMap = (Map<String, Object>) addOnsMap.get(addOnName);
            AddOn addOn = AddOnParser.parseMapToAddOn(addOnName, addOnMap, pricingManager);

            addOns.put(addOnName, addOn);
        }

        pricingManager.setAddOns(addOns);
    }
}
