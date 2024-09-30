package io.github.isagroup.services.updaters;

import java.util.Map;

import io.github.isagroup.exceptions.UpdateException;
import io.github.isagroup.exceptions.VersionException;

public abstract class VersionUpdater implements Updater {

    private final Version target;
    private final Updater versionUpdater;

    public VersionUpdater(Version target, Updater updater) {
        this.target = target;
        this.versionUpdater = updater;
    }

    @Override
    public Map<String, Object> update(Map<String, Object> configFile) throws UpdateException {

        if (this.versionUpdater == null) {
            return configFile;
        }

        if (target == null) {
            throw new UpdateException("Target version was not specified in the constructor");
        }

        try {
            if (Version.version(configFile.get("version")).equals(target)) {
                return configFile;
            }
        } catch (VersionException e) {
            throw new UpdateException(e.getMessage());
        }

        return this.versionUpdater.update(configFile);
    }
}
