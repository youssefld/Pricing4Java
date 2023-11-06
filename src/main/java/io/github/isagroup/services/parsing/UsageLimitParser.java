package io.github.isagroup.services.parsing;

import java.util.Map;
import java.util.Set;

import io.github.isagroup.exceptions.InvalidDefaultValueException;
import io.github.isagroup.exceptions.InvalidLinkedFeatureException;
import io.github.isagroup.models.PricingManager;
import io.github.isagroup.models.UsageLimit;
import io.github.isagroup.models.UsageLimitType;
import io.github.isagroup.models.ValueType;
import io.github.isagroup.models.usagelimittypes.Capacity;
import io.github.isagroup.models.usagelimittypes.Quota;
import io.github.isagroup.models.usagelimittypes.ResponseDriven;
import io.github.isagroup.models.usagelimittypes.TimeDriven;

public class UsageLimitParser {

    private UsageLimitParser(){}

    public static UsageLimit parseMapToFeature(String limitName, Map<String, Object> limitMap, PricingManager pricingManager){

        Set<String> featureKeys = pricingManager.getFeatures().keySet();
        
        try{

            switch (UsageLimitType.valueOf((String)limitMap.get("type"))) {
                
                case CAPACITY:
                    return parseMapToCapacity(limitName, limitMap, featureKeys);
                    
                case QUOTA:
                    return parseMapToQuota(limitName, limitMap, featureKeys);
    
                case RESPONSE_DRIVEN:
                    return parseMapToResponseDriven(limitName, limitMap, featureKeys); 
    
                case TIME_DRIVEN:
                    return parseMapToTimeDriven(limitName, limitMap, featureKeys);
            
                default:
                    return null;
            }
        }catch(IllegalArgumentException e){
            throw new IllegalArgumentException("The usage limit " + limitName + " does not have a supported type. Current type value: " + (String)limitMap.get("type"));
        }
    }

    private static Capacity parseMapToCapacity(String limitName, Map<String, Object> map, Set<String> featureKeys){
        Capacity capacity = new Capacity();

        loadBasicAttributes(capacity, limitName, map, featureKeys);

        return capacity;
    }

    private static Quota parseMapToQuota(String limitName, Map<String, Object> map, Set<String> featureKeys){
        Quota quota = new Quota();

        loadBasicAttributes(quota, limitName, map, featureKeys);

        return quota;
    }

    private static ResponseDriven parseMapToResponseDriven(String limitName, Map<String, Object> map, Set<String> featureKeys){
        ResponseDriven responseDriven = new ResponseDriven();

        loadBasicAttributes(responseDriven, limitName, map, featureKeys);

        return responseDriven;
    }

    private static TimeDriven parseMapToTimeDriven(String limitName, Map<String, Object> map, Set<String> featureKeys){
        TimeDriven timeDriven = new TimeDriven();

        loadBasicAttributes(timeDriven, limitName, map, featureKeys);

        return timeDriven;
    }

    private static void loadBasicAttributes(UsageLimit limit, String limitName, Map<String, Object> map, Set<String> featureKeys){
        limit.setDescription((String) map.get("description"));
        try{
            limit.setValueType(ValueType.valueOf((String) map.get("valueType")));
        }catch(IllegalArgumentException e){
            throw new IllegalArgumentException("The feature " + limitName + "does not have a supported valueType. Current valueType: " + (String) map.get("valueType"));
        }
        try{
            switch (limit.getValueType()) {
                case NUMERIC:
                    limit.setDefaultValue(map.get("defaultValue"));
                    if (!(limit.getDefaultValue() instanceof Integer || limit.getDefaultValue() instanceof Double || limit.getDefaultValue() instanceof Long)){
                        throw new InvalidDefaultValueException("The usageLimit " + limitName + " does not have a valid defaultValue. Current valueType:" + limit.getValueType().toString() + "; Current defaultValue: " + map.get("defaultValue").toString());
                    }
                    break;
                case BOOLEAN:
                    limit.setDefaultValue((boolean) map.get("defaultValue"));
                    break;
                case TEXT:    
                    limit.setDefaultValue((String) map.get("defaultValue"));
                    break;
            }
        }catch(ClassCastException e){
            throw new InvalidDefaultValueException("The feature " + limitName + " does not have a valid defaultValue. Current valueType:" + limit.getValueType().toString() + "; Current defaultValue: " + map.get("defaultValue").toString());
        }
        limit.setUnit((String) map.get("unit"));

        if (map.get("linkedFeature") == null){
            limit.setLinkedFeature(null);
        }else{

            String linkedFeature = (String) map.get("linkedFeature");

            if (featureKeys.contains(linkedFeature)){
                limit.setLinkedFeature(linkedFeature);
            }else{
                throw new InvalidLinkedFeatureException("The feature " + limitName + " is linked to a nonexistent feature. Current linkedFeature: " + linkedFeature);
            }
        }
    }

}
