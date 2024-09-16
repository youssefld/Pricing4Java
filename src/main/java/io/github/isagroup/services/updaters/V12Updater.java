package io.github.isagroup.services.updaters;

import java.util.Map;

public class V12Updater extends VersionUpdater {

    public V12Updater(Map<String, Object> yamlFile) {
        super(Version.V1_2, new V11Updater(yamlFile), yamlFile);
    }

    @Override
    public Map<String, Object> update() throws Exception {
        Map<String, Object> newFile = super.update();
        newFile.put("version", "1.2");
        return newFile;
    }

}
