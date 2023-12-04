package io.github.isagroup.models;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddOn {
    private List<String> availableFor;
    private Object price;
    private Double monthlyPrice;
    private Double annualPrice;
    private String unit;
    private Map<String, Feature> features;
    private Map<String, UsageLimit> usageLimits;
    private Map<String, UsageLimit> usageLimitsExtensions;
}
