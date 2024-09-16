package io.github.isagroup.models;

import java.time.LocalDate;
import java.util.Date;

import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import io.github.isagroup.services.updaters.Version;

/**
 * Object to model pricing configuration
 */
@Getter
@Setter
@EqualsAndHashCode
public class PricingManager {

    private Version version;
    private String saasName;
    private LocalDate createdAt;
    private Date starts;
    private Date ends;
    private String currency;
    private Boolean hasAnnualPayment;
    private Map<String, Feature> features;
    private Map<String, UsageLimit> usageLimits;
    private Map<String, Plan> plans;
    private Map<String, AddOn> addOns;

}
