package io.github.isagroup.services.updaters;

import java.util.Map;

public class YamlUpdater {

    private final Map<String, Object> configFile;

    private Updater updater;

    public YamlUpdater(Map<String, Object> configFile) {
        this.configFile = configFile;
        this.updater = new BaseUpdater(configFile);
    }

    public Map<String, Object> update(Version version) throws Exception {

        switch (version) {
            case V1_0 -> this.updater = new BaseUpdater(configFile);
            case V1_1 -> this.updater = new V11Updater(configFile);
            case V1_2 -> this.updater = new V12Updater(configFile);
        }

        return updater.update();
    }
}
