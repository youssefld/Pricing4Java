package io.github.isagroup.models;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PricingManager {
    public Map<String, Plan> plans;
    public Map<String, Evaluator> evaluators;
}
