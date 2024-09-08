package io.github.isagroup.services.serializer;

import java.util.Map;

import io.github.isagroup.models.PricingManager;

public abstract class VersionSerializer implements Serializable {

    private Serializable serializer;

    public VersionSerializer(Serializable serializer) {
        this.serializer = serializer;
    }

    @Override
    public Map<String, Object> serialize(PricingManager pricingManager) {
        return this.serializer.serialize(pricingManager);
    }

}
