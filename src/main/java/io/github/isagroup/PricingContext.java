package io.github.isagroup;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.isagroup.models.Evaluator;
import io.github.isagroup.models.Feature;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.yaml.YamlUtils;

@Component
public abstract class PricingContext {

    public String getConfigFilePath(){
        return "";
    };

    public String getJwtSecret(){
        return "jwtSecret";
    };

    public int getJwtExpiration(){
        return 86400000;
    };

    public abstract Map<String, Object> getUserContext();

    public abstract String getUserPlan();

    public abstract Object getUserAuthorities();

    public final Map<String, Object> getPlanContext(){

        Map<String, Feature> features = this.getPricingManager().getPlans().get(this.getUserPlan()).getFeatures();

        return features.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue()));
    };

    public final Map<String, Evaluator> getEvaluators(){
        return this.getPricingManager().getEvaluators();
    }

    public final PricingManager getPricingManager(){
        return YamlUtils.retrieveManagerFromYaml(this.getConfigFilePath());
    }
}
