package io.github.isagroup.services.updaters;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.github.isagroup.exceptions.UpdateException;

public class V10ToV11Updater extends VersionUpdater {

    public V10ToV11Updater(Updater updater) {
        super(Version.V1_1, updater);
    }

    @Override
    public Map<String, Object> update(Map<String, Object> configFile) throws UpdateException {

        Map<String, Object> yamlFile = super.update(configFile);

        yamlFile.put("version", "1.1");

        if ((yamlFile.get("day") != null && yamlFile.get("day") instanceof Integer) &&
            (yamlFile.get("month") != null && yamlFile.get("month") instanceof Integer) &&
            (yamlFile.get("year") != null && yamlFile.get("year") instanceof Integer)){
            int day = (int) yamlFile.get("day");
            int month = (int) yamlFile.get("month");
            int year = (int) yamlFile.get("year");

            yamlFile.remove("day");
            yamlFile.remove("month");
            yamlFile.remove("year");
            yamlFile.put("createdAt", LocalDate.of(year, month, day).toString());
        }

        if (yamlFile.get("features") instanceof Map) {
            removeServerExpression((Map<String, Object>) yamlFile.get("features"));
        }


        return yamlFile;
    }

    private void removeServerExpression(Map<String, Object> features) throws UpdateException {
        for (Entry<String, Object> feature : features.entrySet()) {

            if (!(feature.getValue() instanceof Map)) {
                continue;
            }

            modifyFeature((Map<String,Object>) feature.getValue());

        }

    }

    private void modifyFeature(Map<String, Object> attributes) {
        if (attributes.get("expression") != null &&
            attributes.get("serverExpression") != null &&
            attributes.get("expression").equals(attributes.get("serverExpression"))) {
            attributes.remove("serverExpression");
        }
    }

}
