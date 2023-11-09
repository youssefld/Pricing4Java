package io.github.isagroup.services.parsing;

import java.util.HashMap;
import java.util.Map;

import io.github.isagroup.models.AddOn;
import io.github.isagroup.models.Feature;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.models.UsageLimit;

public class PricingManagerParser {

    private PricingManagerParser(){}

    public static PricingManager parseMapToPricingManager(Map<String, Object> map){
        
        PricingManager pricingManager = new PricingManager();
        
        setBasicAttributes(map, pricingManager);
        setFeatures(map, pricingManager);
        setUsageLimits(map, pricingManager);
        setPlans(map, pricingManager);
        setAddOns(map, pricingManager);

        return pricingManager;
    }

    private static void setBasicAttributes(Map<String, Object> map, PricingManager pricingManager){
        pricingManager.setSaasName((String) map.get("saasName"));
        pricingManager.setDay((int) map.get("day"));
        pricingManager.setMonth((int) map.get("month"));
        pricingManager.setYear((int) map.get("year"));
        pricingManager.setCurrency((String) map.get("currency"));
        pricingManager.setHasAnnualPayment((boolean) map.get("hasAnnualPayment"));
    }

    private static void setFeatures(Map<String, Object> map, PricingManager pricingManager){
        Map<String, Feature> pricingFeatures = new HashMap<>();
        Map<String, Object> featuresMap = (Map<String, Object>) map.get("features");
        
        for (String featureName: featuresMap.keySet()){
            Map<String, Object> featureMap = (Map<String, Object>) featuresMap.get(featureName);
            Feature feature = FeatureParser.parseMapToFeature(featureName, featureMap);
            pricingFeatures.put(featureName, feature);
        }

        pricingManager.setFeatures(pricingFeatures);
    }

    private static void setUsageLimits(Map<String, Object> map, PricingManager pricingManager){
        Map<String, Object> usageLimitsMap = (Map<String, Object>) map.get("usageLimits");
        Map<String, UsageLimit> usageLimits = new HashMap<>();

        for (String limitName: usageLimitsMap.keySet()){
            Map<String, Object> limitMap = (Map<String, Object>) usageLimitsMap.get(limitName);
            UsageLimit limit = UsageLimitParser.parseMapToFeature(limitName, limitMap, pricingManager);

            usageLimits.put(limitName, limit);
        }

        pricingManager.setUsageLimits(usageLimits);
    }

    private static void setPlans(Map<String, Object> map, PricingManager pricingManager){
        Map<String, Object> plansMap = (Map<String, Object>) map.get("plans");
        Map<String, Plan> plans = new HashMap<>();

        for (String planName: plansMap.keySet()){
            Map<String, Object> planMap = (Map<String, Object>) plansMap.get(planName);
            Plan plan = PlanParser.parseMapToPlan(planName, planMap, pricingManager);

            plans.put(planName, plan);
        }

        pricingManager.setPlans(plans);
    }

    private static void setAddOns(Map<String, Object> map, PricingManager pricingManager){
        Map<String, Object> addOnsMap = (Map<String, Object>) map.get("addOns");
        Map<String, AddOn> addOns = new HashMap<>();

        if (addOnsMap == null){
            return;
        }

        for (String addOnName: addOnsMap.keySet()){
            Map<String, Object> addOnMap = (Map<String, Object>) addOnsMap.get(addOnName);
            AddOn addOn = AddOnParser.parseMapToAddOn(addOnName, addOnMap, pricingManager);

            addOns.put(addOnName, addOn);
        }

        pricingManager.setAddOns(addOns);
    }
}
