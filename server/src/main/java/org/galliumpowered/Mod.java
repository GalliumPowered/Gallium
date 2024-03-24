package org.galliumpowered;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.galliumpowered.bridge.BridgeImpl;
import org.galliumpowered.command.CommandManager;
import org.galliumpowered.database.Database;
import org.galliumpowered.event.EventDispatcherImpl;
import org.galliumpowered.event.EventManager;
import org.galliumpowered.exceptions.GalliumDatabaseException;
import org.galliumpowered.permission.GroupManager;
import org.galliumpowered.permission.PermissionManager;
import org.galliumpowered.plugin.PluginManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.SQLException;

public class Mod extends Gallium {
    public static final Logger log = LogManager.getLogger();
    public static Config config;
    private static MinecraftServer minecraftServer;

    /**
     * Makes a new Gallium
     */
    public Mod() {
        Gallium.setGallium(this);

        // These need to go before everything else
        this.galliumConfig = new File("config/gallium.conf");
        this.pluginsDirectory = new File("plugins/");
        Gallium.mkdirs();
        config = new Config();
        this.database = new Database(config.getDatabaseConnectionURL());

        try {
            database.open();
        } catch (SQLException e) {
            log.fatal("Could not open database connection", e);
            return;
        }

        this.bridge = new BridgeImpl();
        this.server = new ServerImpl();

        this.commandManager = new CommandManager();
        this.permissionManager = new PermissionManager();
        this.groupManager = new GroupManager();
        this.pluginManager = new PluginManager();
        this.eventManager = new EventManager();
        this.eventDispatcher = new EventDispatcherImpl();
        this.serverProperties = new File("config/server.properties");
        this.bannedIPsFile = new File("data/banned-ips.json");
        this.bannedPlayersFile = new File("data/banned-players.json");
        this.opListFile = new File("data/ops.json");
        this.whitelistFile = new File("data/whitelist.json");
        this.pluginConfigDirectory = new File("config/");

        try {
            database.addGroupsToGroupManager(groupManager);
        } catch (SQLException e) {
            throw new GalliumDatabaseException(e);
        }

        Gallium.renameDefaultFiles();
    }

    public static void setMinecraftServer(MinecraftServer server) {
        minecraftServer = server;
    }

    public static MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }
}

