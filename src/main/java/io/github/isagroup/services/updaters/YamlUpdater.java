package io.github.isagroup.services.updaters;

import java.util.LinkedHashMap;
import java.util.Map;

import io.github.isagroup.exceptions.UpdateException;
import io.github.isagroup.exceptions.VersionException;

public class YamlUpdater {

    private static final Map<Version, Updater> updaters = new LinkedHashMap<>();

    static {
        updaters.put(Version.V1_0, new V10ToV11Updater(null));
        updaters.put(Version.V1_1, new V11ToV20Updater(updaters.get(Version.V1_0)));
    }

    public static void update(Map<String, Object> configFile) throws UpdateException {


        configFile.putIfAbsent("version", "1.0");

        Version version = Version.version(configFile.get("version"));
        if (updaters.get(version) == null) {
            return;
        }

        updaters.get(Version.V1_1).update(configFile);
    }
}
