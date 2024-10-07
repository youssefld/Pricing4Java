package io.github.isagroup.services.updaters;

import java.util.Map;

import io.github.isagroup.exceptions.UpdateException;
import io.github.isagroup.exceptions.VersionException;

public class V11ToV12Updater extends VersionUpdater {

    public V11ToV12Updater(Updater updater) {
        super(Version.V1_1, updater);
    }

    @Override
    public void update(Map<String, Object> configFile) throws UpdateException {

        try {
            if (Version.version(configFile.get("version")).compare(this.getSource()) < 0) {
                super.update(configFile);
            }
        } catch (VersionException e) {
            throw new RuntimeException(e);
        }

        configFile.put("version", "1.2");
    }

}
