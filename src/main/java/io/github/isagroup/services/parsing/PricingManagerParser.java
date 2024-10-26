package io.github.isagroup.services.parsing;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.isagroup.exceptions.PricingParsingException;
import io.github.isagroup.models.AddOn;
import io.github.isagroup.models.Feature;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.models.UsageLimit;

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
        setTags(yamlConfigMap, pricingManager);

        if (pricingManager.getPlans() == null && pricingManager.getAddOns() == null) {
            throw new PricingParsingException("The pricing manager does not have any plans or add ons");
        }

        return pricingManager;
    }

    private static void setBasicAttributes(Map<String, Object> yamlConfigMap, PricingManager pricingManager)
            throws PricingParsingException {

        if (yamlConfigMap.get("saasName") == null) {
            throw new PricingParsingException("SaasName was not defined");
        }

        if (yamlConfigMap.get("day") == null) {
            throw new PricingParsingException("Day of plan was not defined");
        }

        if (yamlConfigMap.get("month") == null) {
            throw new PricingParsingException("Monnth of plan was not defined");
        }

        if (yamlConfigMap.get("year") == null) {
            throw new PricingParsingException("Year of plan was not defined");
        }

        if (yamlConfigMap.get("currency") == null) {
            throw new PricingParsingException("Currency was not defined");
        }

        if (yamlConfigMap.get("hasAnnualPayment") != null) {
            pricingManager.setHasAnnualPayment((Boolean) yamlConfigMap.get("hasAnnualPayment"));
        }

        pricingManager.setSaasName((String) yamlConfigMap.get("saasName"));
        pricingManager.setDay((int) yamlConfigMap.get("day"));
        pricingManager.setMonth((int) yamlConfigMap.get("month"));
        pricingManager.setYear((int) yamlConfigMap.get("year"));
        pricingManager.setCurrency((String) yamlConfigMap.get("currency"));
    }

    private static void setFeatures(Map<String, Object> map, PricingManager pricingManager) {
        Map<String, Feature> pricingFeatures = new LinkedHashMap<>();

        Map<String, Object> featuresMap = new HashMap<>();
        
        try{
            featuresMap = (Map<String, Object>) map.get("features");
        }catch(ClassCastException e){
            throw new PricingParsingException("The features are not defined correctly. It should be a map of features and their options");
        }

        if (featuresMap == null) {
            throw new IllegalArgumentException("The pricing manager does not have any features");
        }

        for (String featureName : featuresMap.keySet()) {

            try{
                Map<String, Object> featureMap = (Map<String, Object>) featuresMap.get(featureName);
                Feature feature = FeatureParser.parseMapToFeature(featureName, featureMap);
                pricingFeatures.put(featureName, feature);
            }catch (ClassCastException e){
                throw new PricingParsingException("The feature " + featureName + " is not defined correctly. All its options must be specified, and it cannot be defined as a key-value pair");
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

        try{
            plansMap = (Map<String, Object>) map.get("plans");
        }catch(ClassCastException e){
            throw new PricingParsingException("The plans are not defined correctly. It should be a map of plans and their options");
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

    private static void setTags(Map<String, Object> map, PricingManager pricingManager) {
        Object tagsObject = map.get("tags");
        if (tagsObject != null) {
            if (!(tagsObject instanceof List)) {
                throw new PricingParsingException("The tags are not defined correctly. It should be a list of strings");
            }
            
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) tagsObject;
            pricingManager.setTags(tags);
        }
    }
}
