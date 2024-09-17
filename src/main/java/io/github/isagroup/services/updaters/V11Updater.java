package io.github.isagroup.services.updaters;

import java.time.LocalDate;
import java.util.Map;
import java.util.Map.Entry;

public class V11Updater extends VersionUpdater {

    public V11Updater(Map<String, Object> yamlFile) {
        super(Version.V1_1, new BaseUpdater(yamlFile), yamlFile);
    }

    @Override
    public Map<String, Object> update() throws Exception {

        Map<String, Object> yamlFile = super.update();

        if (yamlFile.get("day") == null) {
            throw new Exception("day cannot be null");
        }

        if (!(yamlFile.get("day") instanceof Integer)) {
            throw new Exception("day must be an integer");
        }

        if (yamlFile.get("month") == null) {
            throw new Exception("month cannot be null");
        }

        if (!(yamlFile.get("month") instanceof Integer)) {
            throw new Exception("month must be an integer");
        }

        int month = (int) yamlFile.get("month");

        if (!(month >= 1 && month <= 12)) {
            throw new Exception("month is out of range");
        }

        if (yamlFile.get("year") == null) {
            throw new Exception("year cannot be null");
        }

        if (!(yamlFile.get("year") instanceof Integer)) {
            throw new Exception("year must be an integer");
        }

        int day = (int) yamlFile.get("day");
        int year = (int) yamlFile.get("year");

        yamlFile.put("createdAt", LocalDate.of(year, month, day).toString());
        yamlFile.remove("day");
        yamlFile.remove("month");
        yamlFile.remove("year");

        yamlFile.put("version", "1.1");
        yamlFile.put("features", removeServerExpression((Map<String, Object>) yamlFile.get("features")));

        return yamlFile;
    }

    private Map<String, Object> removeServerExpression(Map<String, Object> features) {
        for (Entry<String, Object> feature : features.entrySet()) {
            Map<String, Object> featureAttributes = (Map<String, Object>) feature.getValue();
            String expression = (String) featureAttributes.get("expression");
            String serverExpression = (String) featureAttributes.get("serverExpression");

            if (expression != null && expression.equals(serverExpression)) {
                featureAttributes.remove("serverExpression");
            }

            features.put(feature.getKey(), featureAttributes);

        }

        return features;
    }

}
