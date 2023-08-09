package org.galliumpowered.plugin.inject.modules;

import com.google.inject.AbstractModule;
import org.galliumpowered.plugin.PluginContainer;
import org.galliumpowered.plugin.inject.providers.PluginContainerProvider;
import org.galliumpowered.plugin.inject.providers.PluginLoggerProvider;
import org.apache.logging.log4j.Logger;

public class InjectPluginModule extends AbstractModule {
    private PluginContainer container;
    public InjectPluginModule(PluginContainer container) {
        this.container = container;
    }

    @Override
    protected void configure() {
        bind(Logger.class).toProvider(new PluginLoggerProvider(container.getMetadata()));
        bind(PluginContainer.class).toProvider(new PluginContainerProvider(container));
    }
}
