package io.github.isagroup;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.isagroup.models.Feature;
import io.github.isagroup.models.FeatureType;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.services.yaml.YamlUtils;

@Service
public class PricingService {
    
    @Autowired
    private PricingContext pricingContext;

    // ------------------------- PLAN MANAGEMENT ------------------------- //

    public Plan getPlanFromName(String planName) {
        
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());
        Plan plan = pricingManager.getPlans().get(planName);

        if (plan == null){
            throw new IllegalArgumentException("The plan " + planName + " does not exist in the current pricing configuration");
        }else{
            return plan;
        }
    }

    @Transactional
    public void addPlanToConfiguration(String name, Plan plan){
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Plan> plans = pricingManager.getPlans();

        if (plans.containsKey(name)){
            throw new IllegalArgumentException("The plan " + name + " already exists in the current pricing configuration");
        }else{
            plans.put(name, plan);
            pricingManager.setPlans(plans);
            YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
        }
    }

    @Transactional
    public void addFeatureToConfiguration(String name, Feature feature){
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Feature> features = pricingManager.getFeatures();

        if (features.containsKey(name)){
            throw new IllegalArgumentException("The feature " + name + " does already exist in the current pricing configuration. Check the features");
        }else{
            feature.setValue(null);
            features.put(name, feature);
            pricingManager.setFeatures(features);
        }

        Map<String, Plan> plans = pricingManager.getPlans();

        for(String planName: plans.keySet()){

            Plan plan = plans.get(planName);
            Map<String, Feature> planFeatures = plan.getFeatures();

            if (planFeatures.containsKey(name)){
                throw new IllegalArgumentException("The feature " + name + " does already exist in the current pricing configuration. Check the " + planName + " plan");
            }else{
                Feature newFeature = new Feature();
                newFeature.setValue(features.get(name).getDefaultValue());
                
                planFeatures.put(name, newFeature);
                plan.setFeatures(planFeatures);
                plans.put(planName, plan);
            }
            
        }

        pricingManager.setPlans(plans);

        YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
    }
    
    @Transactional
    public void setPlanFeatureValue(String plan, String attribute, Object value) {
        
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        try{

            Feature selectedPlanFeature = pricingManager.getPlans().get(plan).getFeatures().get(attribute);

            if (selectedPlanFeature == null) {
                throw new IllegalArgumentException("The plan " + plan + " does not have the attribute " + attribute);
            }else if(isNumeric(value) && selectedPlanFeature.getType() == FeatureType.NUMERIC){
                selectedPlanFeature.setValue((Integer) value);
            }else if(isText(value) && selectedPlanFeature.getType() == FeatureType.TEXT){
                selectedPlanFeature.setValue((String) value);
            }else if(isCondition(value) && selectedPlanFeature.getType() == FeatureType.CONDITION){
                selectedPlanFeature.setValue((Boolean) value);
            }else{
                throw new IllegalArgumentException("The value " + value + " is not of the type " + selectedPlanFeature.getType());
            }

            pricingManager.getPlans().get(plan).getFeatures().put(attribute, selectedPlanFeature);

            YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());

        }catch(NullPointerException e){
            throw new IllegalArgumentException("The plan " + plan + " does not exist in the current pricing configuration");
        }

    }

    @Transactional
    public void setPlanPrice(String planName, Double newPrice) {
        
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Plan> plans = pricingManager.getPlans();

        if (!plans.containsKey(planName)){
            throw new IllegalArgumentException("There is no plan with the name " + planName + " in the current pricing configuration");
        }else{
            Plan plan = plans.get(planName);
            plan.setPrice(newPrice);
            plans.put(planName, plan);
            pricingManager.setPlans(plans);
            YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
        }

    }

    @Transactional
    public void setFeatureExpression(String featureName, String expression) {
        
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Feature> features = pricingManager.getFeatures();

        if (!features.containsKey(featureName)){
            throw new IllegalArgumentException("There is no feature with the name " + featureName + " in the current pricing configuration");
        }else{
            Feature feature = features.get(featureName);
            feature.setExpression(expression);
            features.put(featureName, feature);
            pricingManager.setFeatures(features);
            YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
        }

    }

    @Transactional
    public void setFeatureType(String featureName, FeatureType newType) {
        
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Feature> features = pricingManager.getFeatures();

        if (!features.containsKey(featureName)){
            throw new IllegalArgumentException("There is no feature with the name " + featureName + " in the current pricing configuration");
        }else{
            Feature feature = features.get(featureName);
            feature.setType(newType);
            features.put(featureName, feature);
            pricingManager.setFeatures(features);
            YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
        }

    }

    @Transactional
    public void removePlanFromConfiguration(String name){
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Plan> plans = pricingManager.getPlans();

        if (!plans.containsKey(name)){
            throw new IllegalArgumentException("There is no plan with the name " + name + " in the current pricing configuration");
        }else{
            plans.remove(name);
            pricingManager.setPlans(plans);
            YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
        }
    }

    @Transactional
    public void removeFeatureFromConfiguration(String name){
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Feature> features = pricingManager.getFeatures();

        if (!features.containsKey(name)){
            throw new IllegalArgumentException("There is no feature with the name " + name + " in the current pricing configuration");
        }else{
            features.remove(name);
            pricingManager.setFeatures(features);
        }

        Map<String, Plan> plans = pricingManager.getPlans();

        for(String planName: plans.keySet()){

            Plan plan = plans.get(planName);
            Map<String, Feature> planFeatures = plan.getFeatures();

            if (planFeatures.containsKey(name)){
                planFeatures.remove(name);
                plan.setFeatures(planFeatures);
                plans.put(planName, plan);
            }
            
        }

        pricingManager.setPlans(plans);

        YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
    }

    // ------------------------- EVALUATION MANAGEMENT ------------------------- //

    private boolean isNumeric(Object value) {
        return value instanceof Integer || value instanceof Double || value instanceof Float || value instanceof Long || value instanceof Short || value instanceof Byte;
    }

    private boolean isText(Object value) {
        return value instanceof String;
    }

    private boolean isCondition(Object value) {
        return value instanceof Boolean;
    }

}
