package io.github.isagroup.services.serializer;

import java.util.Map;

import io.github.isagroup.models.PricingManager;

public class OneDotZeroSerializer extends VersionSerializer {

    public OneDotZeroSerializer(Serializable serializer) {
        super(serializer);
    }

    @Override
    public Map<String, Object> serialize(PricingManager pricingManager) {

        Map<String, Object> result = super.serialize(pricingManager);
        result.put("day", pricingManager.getCreatedAt().getDayOfMonth());
        result.put("month", pricingManager.getCreatedAt().getMonthValue());
        result.put("year", pricingManager.getCreatedAt().getYear());
        return result;

    }
}
