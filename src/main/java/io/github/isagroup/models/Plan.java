package io.github.isagroup.models;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Object to model pricing plans
 */
@Getter
@Setter
public class Plan {
    public String description;
    public Double price;
    public String currency;
    public Map<String, Feature> features;

    public String toString(){
        return "Plan: " + features.toString();
    }
}
