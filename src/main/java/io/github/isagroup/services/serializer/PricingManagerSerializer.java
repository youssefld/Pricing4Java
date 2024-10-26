package io.github.isagroup.services.serializer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import io.github.isagroup.exceptions.SerializerException;
import io.github.isagroup.models.AddOn;
import io.github.isagroup.models.Feature;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.models.UsageLimit;

public class PricingManagerSerializer {

    private PricingManager pricingManager;
    private Map<String, Object> serializedPricingManager;

    public PricingManagerSerializer(PricingManager pricingManager) {
        this.pricingManager = pricingManager;
        this.serializedPricingManager = new LinkedHashMap<>();
    }

    private void initPricingManagerMetadata() {
        serializedPricingManager.put("saasName", pricingManager.getSaasName());
        serializedPricingManager.put("day", pricingManager.getDay());
        serializedPricingManager.put("month", pricingManager.getMonth());
        serializedPricingManager.put("year", pricingManager.getYear());
        serializedPricingManager.put("currency", pricingManager.getCurrency());
        serializedPricingManager.put("hasAnnualPayment", pricingManager.getHasAnnualPayment());

        if (pricingManager.getTags() != null) {
            serializedPricingManager.put("tags", pricingManager.getTags());
        }

    }

    public Map<String, Object> serialize() throws SerializerException {

        initPricingManagerMetadata();

        if (pricingManager.getFeatures() == null) {
            throw new SerializerException("Features are null. Filling the pricing with features is mandatory.");
        }

        if (pricingManager.getPlans() == null && pricingManager.getAddOns() == null) {
            throw new SerializerException(
                    "Plans and AddOns are null. You have to set at least one of them.");
        }

        serializedPricingManager.put("features", serializeFeatures());
        serializedPricingManager.put("usageLimits", serializeUsageLimits());
        serializedPricingManager.put("plans", serializePlans());
        serializedPricingManager.put("addOns", serializeAddOns().orElse(null));

        return serializedPricingManager;
    }

    private Map<String, Object> serializeFeatures() {

        Map<String, Object> serializedFeatures = new LinkedHashMap<>();
        for (Feature feature : pricingManager.getFeatures().values()) {
            serializedFeatures.put(feature.getName(), feature.serializeFeature());

        }
        return serializedFeatures;
    }

    private Map<String, Object> serializeUsageLimits() {
        Map<String, Object> serializedUsageLimits = new LinkedHashMap<>();

        if (pricingManager.getUsageLimits() == null) {
            return null;
        }

        for (UsageLimit usageLimit : pricingManager.getUsageLimits().values()) {
            serializedUsageLimits.put(usageLimit.getName(), usageLimit.serialize());

        }

        return serializedUsageLimits;

    }

    private Map<String, Object> serializePlans() {
        Map<String, Object> serializedPlans = new LinkedHashMap<>();
        for (Plan plan : pricingManager.getPlans().values()) {
            serializedPlans.put(plan.getName(), plan.serializePlan());
        }
        return serializedPlans;
    }

    public Optional<Map<String, Object>> serializeAddOns() {

        if (pricingManager.getAddOns() == null) {
            return Optional.empty();
        }

        Map<String, Object> serializedAddOns = new LinkedHashMap<>();
        for (AddOn addOn : pricingManager.getAddOns().values()) {
            serializedAddOns.put(addOn.getName(), addOn.serializeAddOn());
        }
        return Optional.of(serializedAddOns);
    }
}
