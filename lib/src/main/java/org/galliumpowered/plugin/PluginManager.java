package org.galliumpowered.plugin;

import org.galliumpowered.Gallium;
import org.galliumpowered.exceptions.PluginLoadFailException;
import org.galliumpowered.plugin.loader.PluginLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;

public class PluginManager {
    public static boolean loadTestPlugin = false;
    private ArrayList<PluginContainer> plugins = new ArrayList<>();
    public PluginLoader javaPluginLoader = new PluginLoader();
    private static final Logger log = LogManager.getLogger("Gallium/PluginManager");

    public Optional<PluginContainer> getPluginById(String id) {
        return plugins.stream()
                .filter(container -> container.getMetadata().getId().equalsIgnoreCase(id))
                .findFirst();
    }

    /**
     * Get the plugins on the server
     * @return ArrayList of plugins
     */
    public ArrayList<PluginContainer> getLoadedPlugins() {
        return plugins;
    }

    public void loadPlugins() throws IOException {
        Gallium.getBridge().loadInternalPlugin();
        if (loadTestPlugin) {
            log.info("Enabling test plugin");
            Gallium.getBridge().loadTestPlugin();
        }
        // Load plugins in the plugins directory
        File pluginsDir = Gallium.getPluginsDirectory();
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
        }
        if (pluginsDir.listFiles() == null) {
            log.info("No plugins are installed!");
            return;
        }
        for (File file : pluginsDir.listFiles()) {
            log.info("Loading plugin {}", file.getName());
            try {
                javaPluginLoader.loadPlugin(file).ifPresent(container -> {
                    container.setLifecycleState(PluginLifecycleState.ENABLED);
                    addPlugin(container);
                });

            } catch (Exception e) {
                throw new PluginLoadFailException(e);
            }
        }
    }

    /**
     * Unload all plugins on the server
     */
    @SuppressWarnings("unchecked")
    public void unloadPlugins() {
        for (PluginContainer plugin : (ArrayList<PluginContainer>) plugins.clone()) {
            log.info("Unloading plugin {}", plugin.getMetadata().getId());
            javaPluginLoader.unloadContainer(plugin);
        }
    }

    /**
     * FOR INTERNAL USE ONLY. DO NOT CALL THIS METHOD.
     * Adds a plugin to the ArrayList
     * @param plugin The {@link PluginContainer} instance
     */
    public void addPlugin(PluginContainer plugin) {
        plugins.add(plugin);
    }

    /**
     * FOR INTERNAL USE ONLY. DO NOT CALL THIS METHOD.
     * Removes a plugin from the ArrayList
     * @param plugin The {@link PluginContainer} instance
     */
    public void removePlugin(PluginContainer plugin) {
        plugins.remove(plugin);
    }
}
