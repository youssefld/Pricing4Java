package io.github.isagroup.services.serializer;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import io.github.isagroup.models.PricingManager;

public class OneDotOneSerializer extends VersionSerializer {

    public OneDotOneSerializer(Serializable serializer) {
        super(serializer);
    }

    @Override
    public Map<String, Object> serialize(PricingManager pricingManager) {
        Map<String, Object> res = super.serialize(pricingManager);
        res.put("version", "1.1");
        res.put("createdAt", pricingManager.getCreatedAt().format(DateTimeFormatter.ISO_DATE));
        if (pricingManager.getStarts() != null) {
            res.put("starts", pricingManager.getStarts());
        }

        if (pricingManager.getEnds() != null) {
            res.put("ends", pricingManager.getEnds());
        }
        return res;
    }

}
