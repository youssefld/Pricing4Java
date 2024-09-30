package io.github.isagroup.services.updaters;

import java.util.LinkedHashMap;
import java.util.Map;

import io.github.isagroup.exceptions.UpdateException;
import io.github.isagroup.exceptions.VersionException;

public class YamlUpdater {

    private static final Map<Version, Updater> updaters = new LinkedHashMap<>();

    static {
        updaters.put(Version.V1_0, null);
        updaters.put(Version.V1_1, new V10ToV11Updater(updaters.get(Version.V1_0)));
        updaters.put(Version.V1_2, new V11ToV12Updater(updaters.get(Version.V1_1)));
    }

    public static Map<String, Object> update(Map<String,Object> configFile, Version version) throws UpdateException {

        Updater updater = updaters.get(version);

        if (configFile.get("version") == null) {
            configFile.put("version", "1.0");
        }
        try {
            if (Version.version(configFile.get("version")).equals(version)) {
                return configFile;
            }
        } catch (VersionException e) {
            throw new UpdateException("Cannot update from \"" + configFile.get("version") + "\" to version " + version);
        }

        return updater.update(configFile);
    }
}
