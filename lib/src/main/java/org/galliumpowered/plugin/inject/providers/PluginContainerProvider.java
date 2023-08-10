package org.galliumpowered.plugin.inject.providers;

import com.google.inject.Provider;
import org.galliumpowered.plugin.PluginContainer;

/**
 * I'm not sure why you'd want this in your plugin, but here it is
 */
public class PluginContainerProvider implements Provider<PluginContainer> {
    PluginContainer container;
    public PluginContainerProvider(PluginContainer container) {
        this.container = container;
    }
    @Override
    public PluginContainer get() {
        return container;
    }
}
