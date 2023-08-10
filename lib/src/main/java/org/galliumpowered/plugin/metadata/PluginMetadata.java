package org.galliumpowered.plugin.metadata;

public interface PluginMetadata {
    /**
     * The name of the plugin
     * @return Plugin name
     */
    String getName();

    /**
     * The plugin ID
     * @return Plugin ID
     */
    String getId();

    /**
     * The plugin description
     * @return Plugin description
     */
    String getDescription();

    /**
     * The authors of the plugin
     * @return Plugin authors
     */
    String[] getAuthors();

    /**
     * The plugin version
     * @return Plugin version
     */
    String getVersion();

    /**
     * Main class of the plugin
     * @return Plugin main class
     */
    String getMainClass();
}
