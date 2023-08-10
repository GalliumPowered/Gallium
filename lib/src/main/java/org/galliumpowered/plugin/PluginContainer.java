package org.galliumpowered.plugin;

import com.google.inject.Injector;
import org.galliumpowered.annotation.PluginLifecycleListener;
import org.galliumpowered.exceptions.PluginException;
import org.galliumpowered.plugin.metadata.PluginMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class PluginContainer {

    private Object instance;
    private PluginMetadata metadata;
    private Injector injector;
    private PluginLifecycleState state = PluginLifecycleState.DISABLED;
    private Logger log = LogManager.getLogger("Gallium/PluginContainer");

    /**
     * Gets the instance of the plugin
     * @return Plugin instance
     */
    public Object getInstance() {
        return instance;
    }

    /**
     * Gets the plugin's metadata
     * @return The plugin's {@link PluginMetadata}
     */
    public PluginMetadata getMetadata() {
        return metadata;
    }

    /**
     * Gets the Guice injector used on the container
     * @return Guice injector
     */
    public Injector getInjector() {
        return injector;
    }

    /**
     * Sets the plugin's instance
     * @param instance The instance
     */
    public void setInstance(Object instance) {
        this.instance = instance;
    }

    /**
     * Sets the plugin's metadata
     * @param metadata The {@link PluginMetadata}
     */
    public void setMetadata(PluginMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Gets the class of the plugin
     * @return Plugin's main class as a {@link Class}
     */
    public Class<?> getPluginClass() {
        return instance.getClass();
    }

    /**
     * Sets the plugin container's {@link Injector}
     * @param injector The injector
     */
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    /**
     * Sets the lifecycle state of the container
     * and triggers the lifecycle event in the plugin's main class
     * @param state The state in which the container will enter
     */
    public void setLifecycleState(PluginLifecycleState state) {
        log.info("Plugin {} is transitioning to lifecycle state {}", metadata.getId(), state);
        this.state = state;
        Arrays.stream(getPluginClass().getMethods())
                .filter(method -> method.isAnnotationPresent(PluginLifecycleListener.class))
                .filter(method -> method.getAnnotation(PluginLifecycleListener.class).value() == state)
                .forEach(method -> {
                    try {
                        method.invoke(instance);
                    } catch (Exception e) {
                        throw new PluginException(e);
                    }
                });
    }

    /**
     * Return a plugin logger
     * @return {@link Logger} for the plugin container
     */
    public Logger getLogger() {
        return LogManager.getLogger(metadata.getId());
    }


    /**
     * Get the {@link PluginLifecycleState}
     * @return Plugin lifecycle state
     */
    public PluginLifecycleState getLifecycleState() {
        return state;
    }
}
