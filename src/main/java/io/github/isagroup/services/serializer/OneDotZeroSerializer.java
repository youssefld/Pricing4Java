package io.github.isagroup.services.serializer;

import java.util.Map;

import io.github.isagroup.models.PricingManager;

public class OneDotZeroSerializer extends VersionSerializer {

    private Serializable serializer;

    public OneDotZeroSerializer(Serializable serializer) {
        super(serializer);
    }

    @Override
    public Map<String, Object> serialize(PricingManager pricingManager) {

        Map<String, Object> result = serializer.serialize(pricingManager);
        result.put("version", null);
        result.put("day", pricingManager.getCreatedAt().getDayOfMonth());
        result.put("month", pricingManager.getCreatedAt().getMonthValue());
        result.put("year", pricingManager.getCreatedAt().getYear());
        return result;

    }
}
