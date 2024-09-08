package io.github.isagroup.services.serializer;

import java.util.Map;

import io.github.isagroup.models.PricingManager;

public interface Serializable {

    public Map<String, Object> serialize(PricingManager pricingManager);
}
