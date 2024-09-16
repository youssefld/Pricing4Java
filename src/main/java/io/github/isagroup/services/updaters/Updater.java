package io.github.isagroup.services.updaters;

import java.util.Map;

public interface Updater {
    Map<String, Object> update() throws Exception;
}
