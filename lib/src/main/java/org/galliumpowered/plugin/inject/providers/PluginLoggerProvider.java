package org.galliumpowered.plugin.inject.providers;

import com.google.inject.Provider;
import org.galliumpowered.plugin.metadata.PluginMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PluginLoggerProvider implements Provider<Logger> {
    private PluginMetadata meta;

    public PluginLoggerProvider(PluginMetadata meta) {
        this.meta = meta;
    }

    @Override
    public Logger get() {
        return LogManager.getLogger("plugin/" + meta.getId());
    }
}
