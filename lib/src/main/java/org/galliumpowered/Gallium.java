package org.galliumpowered;

import org.galliumpowered.bridge.Bridge;
import org.galliumpowered.command.CommandManager;
import org.galliumpowered.database.Database;
import org.galliumpowered.event.EventDispatcher;
import org.galliumpowered.event.EventManager;
import org.galliumpowered.pagination.PaginationManager;
import org.galliumpowered.permission.GroupManager;
import org.galliumpowered.permission.PermissionManager;
import org.galliumpowered.plugin.PluginManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public abstract class Gallium {
    private static final Logger log;
    protected static Gallium instance;
    protected Database database;
    protected CommandManager commandManager;
    protected PermissionManager permissionManager;
    protected GroupManager groupManager;
    protected PluginManager pluginManager;
    protected EventManager eventManager;
    protected EventDispatcher eventDispatcher;
    protected PaginationManager paginationManager;
    protected Bridge bridge;
    protected File galliumConfig;
    protected File serverProperties;
    protected File opListFile;
    protected File whitelistFile;
    protected File bannedIPsFile;
    protected File bannedPlayersFile;
    protected File pluginsDirectory;
    protected File pluginConfigDirectory;
    private static boolean pluginsLoaded = false;
    protected Server server;

    static {
        System.out.println("Please wait while the libraries initialize...");
        log = LogManager.getLogger();
    }

    /**
     * Get the database
     * @return Database
     */
    public static Database getDatabase() {
        return instance.database;
    }

    /**
     * Get server
     * @return the server
     */
    public static Server getServer() {
        return instance.server;
    }

    /**
     * Get command manager
     * @return Command manager
     */
    public static CommandManager getCommandManager() {
        return instance.commandManager;
    }

    /**
     * Get permission manager
     * @return Permission manager
     */
    public static PermissionManager getPermissionManager() {
        return instance.permissionManager;
    }

    /**
     * Get the group manager
     * @return the group manager
     */
    public static GroupManager getGroupManager() {
        return instance.groupManager;
    }

    /**
     * Get plugin manager
     */
    public static PluginManager getPluginManager() {
        return instance.pluginManager;
    }

    /**
     * Get event manager
     */
    public static EventManager getEventManager() {
        return instance.eventManager;
    }

    /**
     * Get event dispatcher
     */
    public static EventDispatcher getEventDispatcher() {
        return instance.eventDispatcher;
    }

    /**
     * Get the pagination manager
     * @return Pagination manager
     */
    public static PaginationManager getPaginationManager() {
        return instance.paginationManager;
    }

    /**
     * Get the NMS bridge
     * @return NMS bridge
     */
    public static Bridge getBridge() {
        return instance.bridge;
    }

    public static File getConfig() {
        return instance.galliumConfig;
    }

    /**
     * Get server.properties
     */
    public static File getDefaultProperties() {
        return instance.serverProperties;
    }

    /**
     * Get ops file
     */
    public static File getOpListFile() {
        return instance.opListFile;
    }

    /**
     * Get whitelist file
     */
    public static File getWhitelistFile() {
        return instance.whitelistFile;
    }

    /**
     * Get banned IPS file
     */
    public static File getBannedIPsFile() {
        return instance.bannedIPsFile;
    }

    /**
     * Get banned players file
     */
    public static File getBannedPlayersFile() {
        return instance.bannedPlayersFile;
    }

    /**
     * Get the server plugins directory
     */
    public static File getPluginsDirectory() {
        return instance.pluginsDirectory;
    }

    /**
     * Get the plugins config directory
     * @return Plugins config directory
     */
    public static File getPluginConfigDirectory() {
        return instance.pluginConfigDirectory;
    }

    /**
     * FOR INTERNAL USE ONLY
     * Set instance
     * @param gallium instance
     */
    public static void setGallium(Gallium gallium) {
        if (instance == null) {
            instance = gallium;
        }
    }

    /**
     * FOR INTERNAL USE ONLY
     * Set server
     * @param server server
     */
    public static void setServer(Server server) {
        if (instance.server == null) {
            instance.server = server;
        }
    }

    /**
     * Enable the plugins
     * Ensures that plugins aren't already loaded
     */
    public static void loadPlugins() {
        if (!pluginsLoaded) {
            log.info("Loading plugins");
            try {
                getPluginManager().loadPlugins();
                pluginsLoaded = true;
            } catch (IOException e) {
                log.error("Encounted error while loading plugins!", e);
            }
        } else {
            log.debug("Plugins are already loaded - not loading");
        }
    }

    /**
     * Get gallium API version
     * @return API version
     */
    public static String getVersion() {
        return "1.2.0-beta.0";
    }

    /**
     * FOR INTERNAL USE ONLY
     * Make config and data directories
     */
    public static void mkdirs() {
        final File configDir = new File("config/");
        final File dataDir = new File("data/");

        if (!configDir.exists()) configDir.mkdirs();
        if (!dataDir.exists()) dataDir.mkdirs();
        if (!instance.pluginsDirectory.exists()) instance.pluginsDirectory.mkdirs();
    }

    /**
     * FOR INTERNAL USE ONLY
     * Rename default Minecraft config files to the place Gallium stores them
     */
    public static void renameDefaultFiles() {
        // Rename default files
        File defaultProperties = new File("server.properties");
        File bannedIps = new File("banned-ips.json");
        File bannedPlayers = new File("banned-players.json");
        File ops = new File("ops.json");
        File whitelist = new File("whitelist.json");

        if (defaultProperties.exists()) {
            log.info("Moving server.properties");
            log.info("If you ever want to move from Gallium to another server mod, you will have to move this file back.");
            boolean moved = defaultProperties.renameTo(defaultProperties);
            if (!moved) {
                log.error("Could not move server.properties! Please do this manually");
                System.exit(1);
            }
        }
        if (bannedIps.exists()) {
            bannedIps.renameTo(bannedIps);
        }
        if (bannedPlayers.exists()) {
            bannedPlayers.renameTo(bannedPlayers);
        }
        if (ops.exists()) {
            ops.renameTo(ops);
        }
        if (whitelist.exists()) {
            whitelist.renameTo(whitelist);
        }
    }
}
