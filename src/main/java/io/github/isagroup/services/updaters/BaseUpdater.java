package io.github.isagroup.services.updaters;

import java.util.Map;

public class BaseUpdater implements Updater {

    private final Map<String, Object> yaml;

    public BaseUpdater(Map<String, Object> yaml) {
        this.yaml = yaml;
    }

    @Override
    public Map<String, Object> update() throws Exception {
        return this.yaml;
    }

}
