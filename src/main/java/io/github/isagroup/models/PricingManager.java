package io.github.isagroup.models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Object to model pricing configuration
 */
@Getter
@Setter
@EqualsAndHashCode
public class PricingManager {
    private String saasName;
    private int day;
    private int month;
    private int year;
    private String currency;
    private Boolean hasAnnualPayment;
    private List<String> tags;
    private Map<String, Feature> features;
    private Map<String, UsageLimit> usageLimits;
    private Map<String, Plan> plans;
    private Map<String, AddOn> addOns;

    public List<String> getPlanNames() {
        return List.copyOf(this.plans.keySet());
    }

    public Map<String, Object> getPlanUsageLimits(String planName) {
        Map<String, Object> usageLimitsContext = new LinkedHashMap<>();
        Map<String, UsageLimit> planUsageLimits = this.plans.get(planName).getUsageLimits();

        Map<String, UsageLimit> defaultUsageLimits = this.usageLimits;

        for (String usageLimitName : defaultUsageLimits.keySet()) {
            Object defaultUsageLimitValue = this.usageLimits.get(usageLimitName);
            Object planUsageLimitValue = planUsageLimits.get(usageLimitName);
            boolean planUsageLimitOverwritesDefaultUsageLimit = planUsageLimitValue != null;

            Object currentValue = defaultUsageLimitValue;
            if (planUsageLimitOverwritesDefaultUsageLimit) {
                currentValue = planUsageLimitValue;
            }
            usageLimitsContext.put(usageLimitName, currentValue);
        }

        return usageLimitsContext;
    }

    /**
     * TODO: Check if this method should be here or where
     * Validate that all the features have tags that are defined in the pricing
     * configuration.
     */ 
    public void validateFeatureTags() {
        for (Feature feature : this.features.values()) {
            if (feature.getTag() != null) {
                if (!this.tags.contains(feature.getTag())) {
                    throw new IllegalArgumentException("Tag " + feature.getTag() + " not found in pricing configuration");
                }
            }
        }
    }

}
