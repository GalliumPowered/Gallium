package org.galliumpowered.plugin.loader;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.galliumpowered.Gallium;
import org.galliumpowered.exceptions.BadPluginException;
import org.galliumpowered.exceptions.PluginLoadFailException;
import org.galliumpowered.plugin.PluginContainer;
import org.galliumpowered.plugin.PluginLifecycleState;
import org.galliumpowered.plugin.inject.modules.InjectPluginModule;
import org.galliumpowered.plugin.metadata.PluginMetadata;
import org.galliumpowered.plugin.metadata.PluginMetadataLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Optional;

/**
 * Loads Java plugins
 */
public class PluginLoader {
    private Logger log = LogManager.getLogger("Gallium/PluginLoader");

    /**
     * Loads a {@link File} as a {@link PluginContainer}
     * @param jar The jar {@link File}
     * @return {@link Optional} of a {@link PluginContainer} instance for the jar
     */
    public Optional<PluginContainer> loadPlugin(@Nonnull File jar) {
        try {
            PluginClassLoader pluginClassLoader = new PluginClassLoader(jar.toPath());
            Optional<PluginMetadata> metaOptional = PluginMetadataLoader.getPluginMetadata(jar);
            if (metaOptional.isPresent()) {
                PluginMetadata meta = metaOptional.get();

                if (meta.getId().equals("gallium") || meta.getId().equals("minecraft") || meta.getId().equals("gtest")) {
                    throw new BadPluginException("Plugin IDs 'gallium', 'minecraft' and 'gtest' are reserved! Found plugin id '" + meta.getId() + "'");
                }

                if (meta.getId().contains(" ")) {
                    throw new BadPluginException("Plugin IDs should not contain spaces! Found: " + meta.getId());
                }

                for (PluginContainer container : Gallium.getPluginManager().getLoadedPlugins()) {
                    if (container.getMetadata().getId().equals(meta.getId())) {
                        throw new PluginLoadFailException("Plugin ID " + meta.getId() + " is already in use!");
                    }
                }

                PluginContainer container = new PluginContainer();

                container.setMetadata(meta);

                Injector injector = Guice.createInjector(new InjectPluginModule(container));
                container.setInjector(injector);

                Class<?> clazz = pluginClassLoader.loadClass(meta.getMainClass());
                container.setInstance(injector.getInstance(clazz));

                return Optional.of(container);
            } else {
                log.error("{} does not seem to have metadata!");
            }
        } catch (Exception e) {
            log.error("Could not load jar {}", jar.getName(), e);
            return Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * Unloads a {@link PluginContainer}
     * @param container The {@link PluginContainer}
     */
    public void unloadContainer(@Nonnull PluginContainer container) {
        container.setLifecycleState(PluginLifecycleState.DISABLED);
        Gallium.getCommandManager().unregisterAllPluginCommands(container.getMetadata());
        Gallium.getPluginManager().removePlugin(container);
    }
}
