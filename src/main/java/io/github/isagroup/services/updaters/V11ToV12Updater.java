package io.github.isagroup.services.updaters;

import java.util.Map;

import io.github.isagroup.exceptions.UpdateException;

public class V11ToV12Updater extends VersionUpdater {

    public V11ToV12Updater(Updater updater) {
        super(Version.V1_2, updater);
    }

    @Override
    public Map<String, Object> update(Map<String, Object> configFile) throws UpdateException {
        Map<String, Object> yamlFile = super.update(configFile);
        yamlFile.put("version", "1.2");
        return yamlFile;
    }

}
