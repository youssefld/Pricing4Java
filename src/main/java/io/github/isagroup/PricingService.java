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
import io.github.isagroup.exceptions.FeatureNotFoundException;
import io.github.isagroup.exceptions.InvalidDefaultValueException;
import io.github.isagroup.exceptions.InvalidValueTypeException;
import io.github.isagroup.models.AddOn;
import io.github.isagroup.models.Feature;
import io.github.isagroup.models.ValueType;
import io.github.isagroup.models.Plan;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.models.UsageLimit;
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
    public void addPlanToConfiguration(String name, Plan plan) {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Plan> plans = pricingManager.getPlans();

        if (name == null) {
            throw new IllegalArgumentException("You have not specified a name for the plan");
        }

        // Serialization depends on the plan name, if plan.name is null serialization
        // will fail
        plan.setName(name);

        if (plans.containsKey(name)) {
            throw new IllegalArgumentException(
                    "The plan " + name + " already exists in the current pricing configuration");
        } else {
            plans.put(name, plan);
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
    public void addFeatureToConfiguration(String name, Feature feature) {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Feature> features = pricingManager.getFeatures();

        if (features.containsKey(name)) {
            throw new IllegalArgumentException("The feature " + name
                    + " does already exist in the current pricing configuration. Check the features");
        } else {
            feature.setValue(null);
            features.put(name, feature);
            pricingManager.setFeatures(features);
        }

        Map<String, Plan> plans = pricingManager.getPlans();

        for (String planName : plans.keySet()) {

            Plan plan = plans.get(planName);
            Map<String, Feature> planFeatures = plan.getFeatures();

            if (planFeatures.containsKey(name)) {
                throw new IllegalArgumentException("The feature " + name
                        + " does already exist in the current pricing configuration. Check the " + planName + " plan");
            } else {
                Feature newFeature;
                try {
                    newFeature = feature.getClass().newInstance();
                    newFeature.setValue(features.get(name).getDefaultValue());
                    newFeature.setName(name);

                    planFeatures.put(name, newFeature);
                    plan.setFeatures(planFeatures);
                    plans.put(planName, plan);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }

        pricingManager.setPlans(plans);

        YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
    }

    /**
     * Modifies a plan's feature value. In order to do that, the plan must exist in
     * the {@link PricingContext}
     * that is being used. A feature with the given feature name must also exist.
     * 
     * @param planName    name of the plan whose feature will suffer the change
     * @param featureName name of the feature that will suffer the change
     * @param value       the new value of the feature. It must be a supported type
     *                    depending on the feature's {@link ValueType} attribute
     * @throws IllegalArgumentException if the plan does not exist in the current
     *                                  pricing configuration
     * @throws IllegalArgumentException if the plan does not contain the feature
     * @throws IllegalArgumentException if the value does not match a supported type
     *                                  depending on the feature's {@link ValueType}
     *                                  attribute
     */
    @Transactional
    public void setPlanFeatureValue(String planName, String featureName, Object value) {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        try {

            Feature selectedPlanFeature = pricingManager.getPlans().get(planName).getFeatures().get(featureName);

            if (selectedPlanFeature == null) {
                throw new IllegalArgumentException(
                        "The plan " + planName + " does not have the feature " + featureName);
            } else if (isNumeric(value) && selectedPlanFeature.getValueType() == ValueType.NUMERIC) {
                selectedPlanFeature.setValue((Integer) value);
            } else if (isText(value) && selectedPlanFeature.getValueType() == ValueType.TEXT) {
                selectedPlanFeature.setValue((String) value);
            } else if (isCondition(value) && selectedPlanFeature.getValueType() == ValueType.BOOLEAN) {
                selectedPlanFeature.setValue((Boolean) value);
            } else {
                throw new IllegalArgumentException(
                        "The value " + value + " is not of the type " + selectedPlanFeature.getValueType());
            }

            pricingManager.getPlans().get(planName).getFeatures().put(featureName, selectedPlanFeature);

            YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());

        } catch (NullPointerException e) {
            throw new IllegalArgumentException(
                    "The plan " + planName + " does not exist in the current pricing configuration");
        }

    }

    /**
     * Modifies a plan's price. In order to do that, the plan must exist in the
     * {@link PricingContext} that is being used.
     * 
     * @param planName name of the plan whose price will suffer the change
     * @param newPrice the new price value of the plan
     * @throws IllegalArgumentException if the plan does not exist in the current
     *                                  pricing configuration
     */
    @Transactional
    public void setPlanPrice(String planName, Double newPrice) {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Plan> plans = pricingManager.getPlans();

        if (!plans.containsKey(planName)) {
            throw new IllegalArgumentException(
                    "There is no plan with the name " + planName + " in the current pricing configuration");
        } else {
            Plan plan = plans.get(planName);
            plan.setMonthlyPrice(newPrice);
            plans.put(planName, plan);
            pricingManager.setPlans(plans);
            YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
        }

    }

    /**
     * Modifies a feature's expression. In order to do that, the feature must exist
     * in the {@link PricingContext} that is being used.
     * 
     * @param featureName name of the feature whose expression will suffer the
     *                    change
     * @param expression  the new expression to evaluate the feature
     * @throws IllegalArgumentException if the feature does not exist in the current
     *                                  pricing configuration
     */
    @Transactional
    public void setFeatureExpression(String featureName, String expression) {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Feature> features = pricingManager.getFeatures();

        if (!features.containsKey(featureName)) {
            throw new IllegalArgumentException(
                    "There is no feature with the name " + featureName + " in the current pricing configuration");
        } else {
            Feature feature = features.get(featureName);
            feature.setExpression(expression);
            features.put(featureName, feature);
            pricingManager.setFeatures(features);
            YamlUtils.writeYaml(pricingManager, pricingContext.getConfigFilePath());
        }

    }

    /**
     * Modifies a feature's type. In order to do that, the feature must exist in the
     * {@link PricingContext} that is being used.
     * 
     * @param featureName name of the feature whose type will suffer the change
     * @param newType     the new type of the feature
     * @throws IllegalArgumentException if the feature does not exist in the current
     *                                  pricing configuration
     */
    @Transactional
    public void setFeatureValueType(String featureName, ValueType newType) {

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Feature> features = pricingManager.getFeatures();

        if (!features.containsKey(featureName)) {
            throw new IllegalArgumentException(
                    "There is no feature with the name " + featureName + " in the current pricing configuration");
        }

        Feature feature = features.get(featureName);
        feature.setValueType(newType);
        feature.setDefaultValue(DEFAULT_VALUES.get(newType));
        feature.setExpression("");
        feature.setServerExpression("");
        features.put(featureName, feature);

        Map<String, Plan> plans = pricingManager.getPlans();

        for (Entry<String, Plan> planEntry : plans.entrySet()) {
            planEntry.getValue().getFeatures().put(featureName, feature);
        }

        pricingManager.setFeatures(features);
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

        validateAndFormatUsageLimit(usageLimit);

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
                    "There is no usage limit with the name " + previousUsageLimitName + " in the current pricing configuration");
        }

        validateAndFormatUsageLimit(usageLimit);

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

        validateAndFormatAddOn(addOn);

        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, AddOn> addOns = pricingManager.getAddOns();

        if (addOns == null){
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
        
        validateAndFormatAddOn(addOn);
        
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

    // ------------------------- EVALUATION MANAGEMENT ------------------------- //

    private boolean isNumeric(Object value) {
        return value instanceof Integer || value instanceof Double || value instanceof Float || value instanceof Long
                || value instanceof Short || value instanceof Byte;
    }

    private boolean isText(Object value) {
        return value instanceof String;
    }

    private boolean isCondition(Object value) {
        return value instanceof Boolean;
    }

    // ------------------------- VALIDATORS ------------------------- //

    private void validateAndFormatUsageLimit(UsageLimit usageLimit) {

        String item = "usage limit";

        validateName(usageLimit.getName(), item);
        if (usageLimit.getName().contains(" ")) {
            usageLimit.setName(parseStringToCamelCase(usageLimit.getName()));
        }
        validateValueType(usageLimit.getValueType(), item);
        validateDefaultValue(usageLimit.getDefaultValue(), usageLimit.getValueType(), item);
        validateUnit(usageLimit.getUnit(), item);
        validateAllFeaturesExist(usageLimit.getLinkedFeatures());

    }

    private void validateAndFormatAddOn(AddOn addOn) {

        String item = "add-on";

        validateName(addOn.getName(), item);
        
        if (addOn.getName().contains(" ")) {
            addOn.setName(parseStringToCamelCase(addOn.getName()));
        }

        validatePrice(addOn, item);
        validateUnit(addOn.getUnit(), item);
        if (addOn.getFeatures() != null)
            validateAllFeaturesExist(addOn.getFeatures().keySet().stream().collect(Collectors.toList()));
        if (addOn.getUsageLimits() != null)
            validateAllUsageLimitsExist(addOn.getUsageLimits().keySet().stream().collect(Collectors.toList()), item);
        if (addOn.getUsageLimitsExtensions() != null)
            validateAllUsageLimitsExist(addOn.getUsageLimitsExtensions().keySet().stream().collect(Collectors.toList()),
                    item);

    }

    private void validateName(String name, String item) {

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("The " + item + " name must not be null or empty");
        }

        if (name.length() < 3) {
            throw new IllegalArgumentException("The " + item + " name must have at least 3 characters");
        }

        if (name.length() > 50) {
            throw new IllegalArgumentException("The " + item + " name must have at most 50 characters");
        }
    }

    private void validatePrice(AddOn addOn, String item) {
        if (addOn.getPrice() == null && addOn.getMonthlyPrice() == null && addOn.getAnnualPrice() == null) {
            throw new IllegalArgumentException(
                    "Either an " + item + " price or a monthlyPrice/annualPrice configuration must be specified");
        }

        if ((addOn.getMonthlyPrice() != null || addOn.getAnnualPrice() != null) && addOn.getPrice() != null) {
            throw new IllegalArgumentException(
                    "You cannot specify both a price and a monthlyPrice/annualPrice configuration");
        }

        if (addOn.getPrice() != null && !(addOn.getPrice() instanceof Double)) {
            throw new IllegalArgumentException("The " + item + " price must be a double");
        }

        if (addOn.getMonthlyPrice() != null && !(addOn.getMonthlyPrice() instanceof Double)) {
            throw new IllegalArgumentException("The " + item + " monthlyPrice must be a double");
        }

        if (addOn.getAnnualPrice() != null && !(addOn.getAnnualPrice() instanceof Double)) {
            throw new IllegalArgumentException("The " + item + " annualPrice must be a double");
        }

        if (addOn.getMonthlyPrice() != null && addOn.getAnnualPrice() == null
                || addOn.getAnnualPrice() != null && addOn.getMonthlyPrice() == null) {
            throw new IllegalArgumentException(
                    "You must specify both a monthlyPrice and an annualPrice in a monthlyPrice/annualPrice configuration");
        }
    }

    private void validateValueType(ValueType valueType, String item) {
        if (valueType == null) {
            throw new InvalidValueTypeException("The " + item + " valueType must not be null");
        }

        if (valueType != ValueType.BOOLEAN && valueType != ValueType.NUMERIC && valueType != ValueType.TEXT) {
            throw new InvalidValueTypeException("The " + item + " valueType must be either BOOLEAN, NUMERIC or TEXT");
        }
    }

    private void validateDefaultValue(Object defaultValue, ValueType valueType, String item) {
        if (defaultValue == null) {
            throw new InvalidDefaultValueException("The " + item + " defaultValue must not be null");
        }

        if (valueType == ValueType.BOOLEAN && !(defaultValue instanceof Boolean)) {
            throw new InvalidDefaultValueException(
                    "The " + item + " defaultValue must be a boolean if valueType is BOOLEAN");
        }

        if (valueType == ValueType.NUMERIC && !(defaultValue instanceof Integer)) {
            throw new InvalidDefaultValueException(
                    "The " + item + " defaultValue must be an integer if valueType is NUMERIC");
        }

        if (valueType == ValueType.TEXT && !(defaultValue instanceof String)) {
            throw new InvalidDefaultValueException(
                    "The " + item + " defaultValue must be a string if valueType is TEXT");
        }
    }

    private void validateUnit(String unit, String item) {
        if (unit == null || unit.length() > 50) {
            throw new IllegalArgumentException(
                    "The " + item + " unit must have at most 10 characters and cannot be null");
        }
    }

    private void validateAllFeaturesExist(List<String> featureNames) {

        PricingManager pricingManager = pricingContext.getPricingManager();

        for (String featureName : featureNames) {
            if (!pricingManager.getFeatures().containsKey(featureName)) {
                throw new FeatureNotFoundException("The feature " + featureName
                        + " to which you're trying to attach an usage limit does not exist within the pricing");
            }
        }
    }

    private void validateAllUsageLimitsExist(List<String> usageLimitNames, String item) {

        PricingManager pricingManager = pricingContext.getPricingManager();

        for (String usageLimitName : usageLimitNames) {
            if (!pricingManager.getUsageLimits().containsKey(usageLimitName)) {
                throw new IllegalArgumentException("The usage limit " + usageLimitName
                        + " to which you're trying to attach an " + item + " does not exist within the pricing");
            }
        }
    }

    private String parseStringToCamelCase(String text) {
        String[] words = text.split(" ");
        String camelCase = words[0].toLowerCase();

        for (int i = 1; i < words.length; i++) {
            camelCase += words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
        }

        return camelCase;
    }

}
