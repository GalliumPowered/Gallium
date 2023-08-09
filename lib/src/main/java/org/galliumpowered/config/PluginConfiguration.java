package org.galliumpowered.config;

import org.galliumpowered.Gallium;
import org.galliumpowered.plugin.PluginContainer;

import java.nio.file.Path;

public class PluginConfiguration extends DefaultConfiguration {
    public PluginConfiguration(PluginContainer container) {
        super(Path.of(Gallium.getPluginConfigDirectory().getAbsolutePath(), container.getMetadata().getId()), "config.cfg");
    }
}
