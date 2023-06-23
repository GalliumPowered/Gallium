package net.zenoc.gallium;

import net.zenoc.gallium.Config;
import net.zenoc.gallium.Gallium;
import net.zenoc.gallium.commandsys.CommandManager;
import net.zenoc.gallium.database.Database;
import net.zenoc.gallium.event.EventDispatcherImpl;
import net.zenoc.gallium.eventsys.EventManager;
import net.zenoc.gallium.exceptions.GalliumDatabaseException;
import net.zenoc.gallium.permissionsys.GroupManager;
import net.zenoc.gallium.permissionsys.PermissionManager;
import net.zenoc.gallium.plugin.PluginManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.SQLException;

public class Mod extends Gallium {
    public static final Logger log = LogManager.getLogger();
    public static Config config;

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
}

