package io.github.isagroup.services.updaters;

import java.util.Map;

public class YamlUpdater implements Updater {

    private Map<String, Object> yaml;

    public YamlUpdater(Map<String, Object> yaml) {
        this.yaml = yaml;
    }

    @Override
    public Map<String, Object> update() throws Exception {
        return this.yaml;
    }

}
