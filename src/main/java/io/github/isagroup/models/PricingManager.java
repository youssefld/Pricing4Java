package io.github.isagroup.models;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Object to model pricing configuration
 */
@Getter
@Setter
public class PricingManager {
    public String saasName;
    public int day;
    public int month;
    public int year;
    public String currency;
    public boolean hasAnnualPayment;
    public Map<String, Plan> plans;
    public Map<String, Feature> features;
    public Map<String, UsageLimit> usageLimits;
    public Map<String, AddOn> addOns;
}
