package io.github.isagroup.services.updaters;

import java.util.Map;

import io.github.isagroup.exceptions.UpdateException;
import io.github.isagroup.exceptions.VersionException;

public class V11ToV20Updater extends VersionUpdater {

    public V11ToV20Updater(Updater updater) {
        super(Version.V1_1, updater);
    }

    @Override
    public void update(Map<String, Object> configFile) throws UpdateException {

        try {
            if (Version.version(configFile.get("version")).compare(this.getSource()) < 0) {
                super.update(configFile);

            }
        } catch (VersionException e) {
            throw new UpdateException(e.getMessage(), configFile);
        }

        updatePlansWithOnlyOnePriceField(configFile);
        configFile.put("version", "2.0");
    }

    private boolean isValidPrice(Object price) {
        return price instanceof Double || price instanceof Long || price instanceof Integer || price instanceof String || price == null;
    }

    private void updatePlansWithOnlyOnePriceField(Map<String,Object> configFile) throws UpdateException {

        if (!(configFile.get("plans") instanceof Map)) {
            return;
        }

        for (Map.Entry<?,?> entry:  ((Map<?, ?>) configFile.get("plans")).entrySet()) {

            if (entry.getValue() == null) {
                throw new UpdateException("plan is null", configFile);
            }

            if (!(entry.getValue() instanceof Map)) {
                throw new UpdateException("plan " + entry.getValue() + "is not a map", configFile);
            }

            Map<String,Object> planAttributes = (Map<String, Object>) entry.getValue();

            if (planAttributes.get("monthlyPrice") == null && planAttributes.get("annualPrice") == null) {
                throw new UpdateException("You have to specify, at least, either a monthlyPrice or an annualPrice for the plan " + entry.getKey(), configFile);
            }

            if (!isValidPrice(planAttributes.get("monthlyPrice")) || !isValidPrice(planAttributes.get("annualPrice"))) {
                throw new UpdateException("Either the monthlyPrice or annualPrice of the plan " + entry.getKey()
                    + " is neither a valid number nor String", configFile);
            }

            if (planAttributes.get("monthlyPrice") != null) {
                planAttributes.put("price", planAttributes.get("monthlyPrice"));
            } else {
                System.out.println("[V20 UPDATER WARNING] plan " + entry.getKey() + " does not have a monthlyPrice but annualPrice instead, keep in mind that we are copying this value to price");
                planAttributes.put("price", planAttributes.get("annualPrice"));
            }
        }

    }

}
