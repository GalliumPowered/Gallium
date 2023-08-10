package org.galliumpowered.internal.plugin.listeners;

import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.EventListener;
import org.galliumpowered.world.entity.Player;
import org.galliumpowered.event.player.PlayerJoinEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public class PlayerJoinListener {
    private static final Logger log = LogManager.getLogger();
    @EventListener
    public void onPlayerJoin(PlayerJoinEvent event) throws SQLException {
        log.debug("Internal plugin player join event called");
        Player player = event.getPlayer();
        if (!Gallium.getDatabase().playerExists(player)) {
            log.info(player.getName() + " with UUID " + player.getUUID() + " is not in database. Is this the first time the player has joined? Adding to database");
            Gallium.getDatabase().insertPlayer(player);
        } else {
            log.debug("In DB");
        }
    }
}
