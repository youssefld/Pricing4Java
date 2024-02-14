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
    public void addFeatureToConfiguration(Feature feature) {
        PricingManager pricingManager = YamlUtils.retrieveManagerFromYaml(pricingContext.getConfigFilePath());

        Map<String, Feature> features = pricingManager.getFeatures();

        if (features.containsKey(feature.getName())) {
            throw new IllegalArgumentException("The feature " + feature.getName()
                    + " does already exist in the current pricing configuration. Check the features");
        } else {
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

        validateAndFormatFeature(feature);

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

        validateAndFormatPlan(plan);

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
                    "There is no usage limit with the name " + previousUsageLimitName
                            + " in the current pricing configuration");
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

    private void validateAndFormatFeature(Feature feature) {
        String item = "feature " + feature.getName();

        validateName(feature.getName(), item);
        if (feature.getName().contains(" ")) {
            feature.setName(parseStringToCamelCase(feature.getName()));
        }
        validateValueType(feature.getValueType(), item);
        validateDefaultValue(feature.getDefaultValue(), feature.getValueType(), item);
        validateValue(feature.getValue(), feature.getValueType(), item);
        validateExpression(feature.getExpression(), item);
        if (feature.getServerExpression() != null) validateExpression(feature.getServerExpression(), item);
        validateValueTypeConsistency(feature.getValueType(), feature.getDefaultValue(), feature.getValue(), feature.getExpression(), feature.getServerExpression(), item);
    }

    private void validateAndFormatUsageLimit(UsageLimit usageLimit) {

        String item = "usage limit " + usageLimit.getName();

        validateName(usageLimit.getName(), item);
        if (usageLimit.getName().contains(" ")) {
            usageLimit.setName(parseStringToCamelCase(usageLimit.getName()));
        }
        validateValueType(usageLimit.getValueType(), item);
        validateDefaultValue(usageLimit.getDefaultValue(), usageLimit.getValueType(), item);
        validateUnit(usageLimit.getUnit(), item);
        validateAllFeaturesExist(usageLimit.getLinkedFeatures());

    }

    private void validateAndFormatPlan(Plan plan) {
        String item = "plan " + plan.getName();

        validateName(plan.getName(), item);
        if (plan.getName().contains(" ")) {
            plan.setName(parseStringToCamelCase(plan.getName()));
        }

        validatePlanPrice(plan);

        validateUnit(plan.getUnit(), item);

        if (plan.getFeatures() != null) {
            validateAllFeaturesExist(plan.getFeatures().keySet().stream().collect(Collectors.toList()));
            for (Feature feature : plan.getFeatures().values()) {
                validateAndFormatFeature(feature);
            }
        }

        if (plan.getUsageLimits() != null) {
            validateAllUsageLimitsExist(plan.getUsageLimits().keySet().stream().collect(Collectors.toList()), item);
            for (UsageLimit usageLimit : plan.getUsageLimits().values()) {
                validateAndFormatUsageLimit(usageLimit);
            }
        }
    }

    private void validateAndFormatAddOn(AddOn addOn) {

        String item = "add-on " + addOn.getName();

        validateName(addOn.getName(), item);

        if (addOn.getName().contains(" ")) {
            addOn.setName(parseStringToCamelCase(addOn.getName()));
        }

        validateAddOnPrice(addOn, item);
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

    private void validatePlanPrice(Plan plan) {
        if (plan.getMonthlyPrice() != null && !(plan.getMonthlyPrice() instanceof Double)) {
            throw new IllegalArgumentException("The plan monthlyPrice must be a double");
        }

        if (plan.getAnnualPrice() != null && !(plan.getAnnualPrice() instanceof Double)) {
            throw new IllegalArgumentException("The plan annualPrice must be a double");
        }

        if (plan.getMonthlyPrice() != null && plan.getAnnualPrice() == null
                || plan.getAnnualPrice() != null && plan.getMonthlyPrice() == null) {
            throw new IllegalArgumentException(
                    "You must specify both a monthlyPrice and an annualPrice in a plan");
        }

        if ((Double) plan.getMonthlyPrice() < (Double) plan.getAnnualPrice()){
            throw new IllegalArgumentException("The monthly price must be greater than the annual price (which must be specified by its monthly price)");
        }
    }

    private void validateAddOnPrice(AddOn addOn, String item) {

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

        if (valueType == ValueType.BOOLEAN && !isCondition(defaultValue)) {
            throw new InvalidDefaultValueException(
                    "The " + item + " defaultValue must be a boolean if valueType is BOOLEAN");
        }

        if (valueType == ValueType.NUMERIC && !isNumeric(defaultValue)) {
            throw new InvalidDefaultValueException(
                    "The " + item + " defaultValue must be one of the supported numeric types if valueType is NUMERIC");
        }

        if (valueType == ValueType.TEXT && !isText(defaultValue)) {
            throw new InvalidDefaultValueException(
                    "The " + item + " defaultValue must be a string if valueType is TEXT");
        }
    }

    /**
     * Validates the value based on the specified value type.
     *
     * @param value     The value to be validated.
     * @param valueType The type of value to be validated (BOOLEAN, NUMERIC, TEXT).
     * @param item      The name of the item being validated.
     * @throws IllegalArgumentException If the value is not of the expected type or
     *                                  if it is null.
     */
    private void validateValue(Object value, ValueType valueType, String item) {

        if (value != null) {
            if (valueType == ValueType.BOOLEAN && !isCondition(value)) {
                throw new IllegalArgumentException("The " + item + " value must be a boolean if valueType is BOOLEAN");
            }

            if (valueType == ValueType.NUMERIC && !isNumeric(value)) {
                throw new IllegalArgumentException(
                        "The " + item + " value must be a one of the supported numeric types if valueType is NUMERIC");
            }

            if (valueType == ValueType.TEXT && !isText(value)) {
                throw new IllegalArgumentException("The " + item + " value must be a string if valueType is TEXT");
            }
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

    private void validateExpression(String expression, String item) {
        if (!(expression instanceof String) || expression.length() > 1000) {
            throw new IllegalArgumentException(
                    "The " + item + " expression must have at most 1000 characters and must be a string");
        }
    }

    private void validateValueTypeConsistency(ValueType valueType, Object defaultValue, Object value, String expression, String serverExpression, String item){
        
        switch (valueType) {
            case BOOLEAN:
                if (!isCondition(defaultValue)) {
                    throw new InvalidDefaultValueException("The defaultValue of " + item + " must be a boolean if valueType is BOOLEAN");
                }

                if (value != null && !isCondition(value)) {
                    throw new IllegalArgumentException("The value of " + item + " must be a boolean if valueType is BOOLEAN");
                }

                if (expression != null && expression.matches(".*[<>=].*")) {
                    throw new IllegalArgumentException("Expression of " + item + " should only include the feature value/defaultValue and the operators '&&', '||' and '!', as it is BOOLEAN");
                }

                if (serverExpression != null && serverExpression.matches(".*[<>=].*")) {
                    throw new IllegalArgumentException("ServerExpression of " + item + " should only include the feature value/defaultValue and the operators '&&', '||' and '!', as it is BOOLEAN");
                }

                break;
            case NUMERIC:

                if (!isNumeric(defaultValue)) {
                    throw new InvalidDefaultValueException("The defaultValue of " + item + " must be a numeric type if valueType is NUMERIC");
                }

                if (value != null && !isNumeric(value)) {
                    throw new IllegalArgumentException("The value of " + item + " must be a numeric type if valueType is NUMERIC");
                }

                if (expression != null && !expression.matches(".*[<>=].*") && !expression.equals("")) {
                    throw new IllegalArgumentException("Expression of " + item + " should include comparison operators such as: <, > or ==, as it is NUMERIC");
                }

                if (serverExpression != null && !serverExpression.matches(".*[<>=].*") && !serverExpression.equals("")) {
                    throw new IllegalArgumentException("ServerExpression of " + item + " should include comparison operators such as: <, > or ==, as it is NUMERIC");
                }

                break;
            case TEXT:

                if (!isText(defaultValue)) {
                    throw new InvalidDefaultValueException("The defaultValue of " + item + " must be a string if valueType is TEXT");
                }

                if (value != null && !isText(value)) {
                    throw new IllegalArgumentException("The value of " + item + " must be a string if valueType is TEXT");
                }

                if (expression != null && expression.matches(".*[<>&|].*")) {
                    throw new IllegalArgumentException("Expression of " + item + " should only include == or != operators, as it is TEXT");
                }

                if (serverExpression != null && serverExpression.matches(".*[<>&|].*")) {
                    throw new IllegalArgumentException("ServerExpression of " + item + " should only include == or != operators, as it is TEXT");
                }

                break;
            default:
                throw new IllegalArgumentException("The valueType must be one of the supported types (BOOLEAN, NUMERIC, TEXT)");
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
