package io.github.isagroup.services.updaters;

import java.util.Map;

public abstract class VersionUpdater implements Updater {

    private Version target;
    private Updater versionUpdater;
    private Map<String, Object> yamlFile;

    public VersionUpdater(Version target, Updater updater, Map<String, Object> yamlFile) {
        this.target = target;
        this.versionUpdater = updater;
        this.yamlFile = yamlFile;

    }

    @Override
    public Map<String, Object> update() throws Exception {

        if (target == null) {
            throw new Exception("Cannot convert to null");
        }

        if (isYamlVersionAndTargetVersionEqual()) {
            return this.yamlFile;
        }
        return this.versionUpdater.update();
    }

    private boolean isYamlVersionAndTargetVersionEqual() throws Exception {
        return Version.version(this.yamlFile.get("version")).equals(this.target);
    }

}
