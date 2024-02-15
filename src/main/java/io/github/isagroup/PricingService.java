package io.github.isagroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.isagroup.exceptions.CloneUsageLimitException;
import io.github.isagroup.models.AddOn;
import io.github.isagroup.models.Feature;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.models.UsageLimit;
import io.github.isagroup.models.ValueType;
import io.github.isagroup.services.yaml.YamlUtils;
import io.github.isagroup.utils.PricingValidators;

/**
 * Service that provides methods to manage the pricing configuration.
 */
@Service
public class PricingService {

    @Autowired
    private PricingContext pricingContext;

    private final Map<ValueType, Object> DEFAULT_VALUES = new HashMap<>();

    public PricingService(PricingContext pricingContext) {
        this.pricingContext = pricingContext;

        DEFAULT_VALUES.put(ValueType.BOOLEAN, false);
        DEFAULT_VALUES.put(ValueType.NUMERIC, 0);
        DEFAULT_VALUES.put(ValueType.TEXT, "");
    }

    // ------------------------- PLAN MANAGEMENT ------------------------- //

    /**
     * Returns the plan of the configuration that matchs the given name.
     * 
     * @param planName name of the plan that must be returned
     * @return The plan of the configuration that matchs the given name
     * @throws IllegalArgumentException if the plan does not exist in the current
     *                                  pricing configuration
     */
    public Plan getPlanFromName(String planName) {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());
        Plan plan = pricingManager.getPlans().get(planName);

        if (plan == null) {
            throw new IllegalArgumentException(
                    "The plan " + planName + " does not exist in the current pricing configuration");
        } else {
            return plan;
        }
    }

    /**
     * Adds a new plan to the current pricing configuration.
     * The plan must not exist and must contain all the
     * features declared on the configuration. It is recommended to use the
     * {@link PricingContext#getFeatures()} method to get the list of features that
     * appear in the configuration.
     * 
     * @param name name of the plan that is going to be added
     * @param plan {@link Plan} object that includes the details of the plan that is
     *             going to be added
     * @throws IllegalArgumentException if the plan does already exist in the
     *                                  current pricing configuration
     */
    @Transactional
    public void addPlanToConfiguration(Plan plan) {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Plan> plans = pricingManager.getPlans();

        if (plans.containsKey(plan.getName())) {
            throw new IllegalArgumentException(
                    "The plan " + plan.getName() + " already exists in the current pricing configuration");
        } else {
            PricingValidators.validateAndFormatPlan(pricingManager, plan);
            plans.put(plan.getName(), plan);
            pricingManager.setPlans(plans);
            YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
        }
    }

    /**
     * Creates a new global feature in the pricing configuration and adds it to all
     * the plans using its default value.
     * 
     * @param name    name of the feature that is going to be added
     * @param feature {@link Feature} object that includes the details of the
     *                feature that is going to be added
     * @throws IllegalArgumentException if the feature does already exist in the
     *                                  current pricing configuration
     */
    @Transactional
    public void addFeatureToConfiguration(Feature feature) {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Feature> features = pricingManager.getFeatures();

        if (features.containsKey(feature.getName())) {
            throw new IllegalArgumentException("The feature " + feature.getName()
                    + " does already exist in the current pricing configuration. Check the features");
        } else {
            PricingValidators.validateAndFormatFeature(feature);
            feature.setValue(null);
            features.put(feature.getName(), feature);
            pricingManager.setFeatures(features);
        }

        YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
    }

    @Transactional
    public void updateFeatureFromConfiguration(String previousName, Feature feature) {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Feature> features = pricingManager.getFeatures();
        Map<String, Plan> plans = pricingManager.getPlans();

        PricingValidators.validateAndFormatFeature(feature);

        if (!features.containsKey(previousName)) {
            throw new IllegalArgumentException(
                    "There is no feature with the name " + previousName + " in the current pricing configuration");
        }

        if (!previousName.equals(feature.getName())) {
            features.remove(previousName);

            for (Entry<String, Plan> planEntry : plans.entrySet()) {

                Object currentValue = planEntry.getValue().getFeatures().get(previousName).getValue();

                planEntry.getValue().getFeatures().remove(previousName);

                try {
                    Feature newFeatureValue = feature.getClass().newInstance();
                    newFeatureValue.setValue(currentValue);
                    newFeatureValue.setName(feature.getName());
                    planEntry.getValue().getFeatures().put(feature.getName(), newFeatureValue);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if (feature.getValueType() != features.get(previousName).getValueType() ||
                feature.getDefaultValue() != features.get(previousName).getDefaultValue()) {

            for (Entry<String, Plan> planEntry : plans.entrySet()) {
                try {
                    planEntry.getValue().getFeatures().remove(previousName);
                } catch (NullPointerException e) {
                    // Do nothing
                }
            }
        }

        features.put(feature.getName(), feature);

        pricingManager.setFeatures(features);
        pricingManager.setPlans(plans);

        YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
    }

    @Transactional
    public void updatePlanFromConfiguration(String previousName, Plan plan) {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Plan> plans = pricingManager.getPlans();

        if (!plans.containsKey(previousName)) {
            throw new IllegalArgumentException(
                    "There is no plan with the name " + previousName + " in the current pricing configuration");
        }

        PricingValidators.validateAndFormatPlan(pricingManager, plan);

        if (!previousName.equals(plan.getName())) {
            plans.remove(previousName);
        }

        plans.put(plan.getName(), plan);

        pricingManager.setPlans(plans);

        YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
    }

    /**
     * Receives a {@link PricingManager} object and writes it to the pricing
     * configuration file.
     * 
     * @param pricingManager the {@link PricingManager} object that models the
     *                       pricing configuration
     */
    @Transactional
    public void setPricingConfiguration(PricingManager pricingManager) {
        YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
    }

    /**
     * Removes a plan from the pricing configuration. In order to do that, it must
     * exist in the {@link PricingContext} that is being used.
     * 
     * @param name the name of the plan that is going to be removed
     * @throws IllegalArgumentException if the plan does not exist in the current
     *                                  pricing configuration
     */
    @Transactional
    public void removePlanFromConfiguration(String name) {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Plan> plans = pricingManager.getPlans();

        if (!plans.containsKey(name)) {
            throw new IllegalArgumentException(
                    "There is no plan with the name " + name + " in the current pricing configuration");
        } else {
            plans.remove(name);
            pricingManager.setPlans(plans);
            YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
        }
    }

    /**
     * Removes a feature from the pricing configuration.
     * In order to do that, it must exist in the {@link PricingContext} that is
     * being used.
     * The method also removes the feature from all the plans that include it.
     * 
     * @param name the name of the feature that is going to be removed
     * @throws IllegalArgumentException if the feature does not exist in the current
     *                                  pricing configuration
     */
    @Transactional
    public void removeFeatureFromConfiguration(String name) {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Feature> features = pricingManager.getFeatures();

        if (!features.containsKey(name)) {
            throw new IllegalArgumentException(
                    "There is no feature with the name " + name + " in the current pricing configuration");
        }

        features.remove(name);
        pricingManager.setFeatures(features);

        Map<String, Plan> plans = pricingManager.getPlans();

        for (String planName : plans.keySet()) {

            Plan plan = plans.get(planName);
            Map<String, Feature> planFeatures = plan.getFeatures();

            if (planFeatures.containsKey(name)) {
                planFeatures.remove(name);
                plan.setFeatures(planFeatures);
                plans.put(planName, plan);
            }

        }

        Map<String, UsageLimit> usageLimits = pricingManager.getUsageLimits();

        for (UsageLimit usageLimit : usageLimits.values()) {
            if (usageLimit.isLinkedToFeature(name)) {
                List<String> newLinkedFeatures = usageLimit.getLinkedFeatures().stream()
                        .filter(featureName -> !featureName.equals(name)).collect(Collectors.toList());
                usageLimit.setLinkedFeatures(newLinkedFeatures.size() == 0 ? null : newLinkedFeatures);
                usageLimits.put(usageLimit.getName(), usageLimit);
            }
        }

        pricingManager.setPlans(plans);
        pricingManager.setUsageLimits(usageLimits);

        YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
    }

    // ------------------------- USAGE LIMIT MANAGEMENT ------------------------- //

    @Transactional
    public void addUsageLimitToConfiguration(UsageLimit usageLimit) {
        PricingManager pricingManager = pricingContext.getPricingManager();

        Map<String, UsageLimit> usageLimits = pricingManager.getUsageLimits();

        PricingValidators.validateAndFormatUsageLimit(pricingManager, usageLimit);

        if (pricingManager.getUsageLimits().containsKey(usageLimit.getName())) {
            throw new CloneUsageLimitException(
                    "An usage limit with the name " + usageLimit.getName()
                            + " already exists within the pricing configuration");
        }

        usageLimits.put(usageLimit.getName(), usageLimit);

        pricingManager.setUsageLimits(usageLimits);

        YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());

    }

    @Transactional
    public void updateUsageLimitFromConfiguration(String previousUsageLimitName, UsageLimit usageLimit) {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, UsageLimit> usageLimits = pricingManager.getUsageLimits();
        List<String> usageLimitsNames = usageLimits.keySet().stream().collect(Collectors.toList());

        if (!usageLimitsNames.contains(previousUsageLimitName)) {
            throw new IllegalArgumentException(
                    "There is no usage limit with the name " + previousUsageLimitName
                            + " in the current pricing configuration");
        }

        PricingValidators.validateAndFormatUsageLimit(pricingManager, usageLimit);

        // Handle different valueTypes and default values

        if (usageLimit.getValueType() != usageLimits.get(usageLimit.getName()).getValueType() ||
                usageLimit.getDefaultValue() != usageLimits.get(usageLimit.getName()).getDefaultValue()) {
            Map<String, Plan> plans = pricingManager.getPlans();

            for (Entry<String, Plan> planEntry : plans.entrySet()) {
                planEntry.getValue().getUsageLimits().remove(previousUsageLimitName);
            }
        }

        if (!previousUsageLimitName.equals(usageLimit.getName())) {
            usageLimits.remove(previousUsageLimitName);
        }

        usageLimits.put(usageLimit.getName(), usageLimit);

        pricingManager.setUsageLimits(usageLimits);

        YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
    }

    @Transactional
    public void removeUsageLimitFromConfiguration(String name) {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, UsageLimit> usageLimits = pricingManager.getUsageLimits();

        if (!usageLimits.containsKey(name)) {
            throw new IllegalArgumentException(
                    "There is no usage limit with the name " + name + " in the current pricing configuration");
        }

        usageLimits.remove(name);
        pricingManager.setUsageLimits(usageLimits);

        YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
    }

    // ------------------------- ADD ONS MANAGEMENT ------------------------- //

    @Transactional
    public void addAddOnToConfiguration(AddOn addOn) {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());
        PricingValidators.validateAndFormatAddOn(pricingManager, addOn);

        Map<String, AddOn> addOns = pricingManager.getAddOns();

        if (addOns == null) {
            addOns = new HashMap<>();
        }

        if (addOns.containsKey(addOn.getName())) {
            throw new IllegalArgumentException(
                    "An add-on with the name " + addOn.getName() + " already exists within the pricing configuration");
        }

        addOns.put(addOn.getName(), addOn);

        pricingManager.setAddOns(addOns);

        YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
    }

    @Transactional
    public void updateAddOnFromConfiguration(String previousName, AddOn addOn) {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());
        List<String> addOnsNames = pricingManager.getAddOns().keySet().stream().collect(Collectors.toList());

        if (!addOnsNames.contains(previousName)) {
            throw new IllegalArgumentException(
                    "There is no add-on with the name " + previousName + " in the current pricing configuration");
        }

        PricingValidators.validateAndFormatAddOn(pricingManager, addOn);

        Map<String, AddOn> addOns = pricingManager.getAddOns();

        if (!previousName.equals(addOn.getName())) {
            addOns.remove(previousName);
        }

        addOns.put(addOn.getName(), addOn);

        pricingManager.setAddOns(addOns);

        YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
    }

    @Transactional
    public void removeAddOnFromConfiguration(String addOnName) {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, AddOn> addOns = pricingManager.getAddOns();

        if (!addOns.containsKey(addOnName)) {
            throw new IllegalArgumentException(
                    "There is no add-on with the name " + addOnName + " in the current pricing configuration");
        }

        addOns.remove(addOnName);
        pricingManager.setAddOns(addOns);

        YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
    }
}
