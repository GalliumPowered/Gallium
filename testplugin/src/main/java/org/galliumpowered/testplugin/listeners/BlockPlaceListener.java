package org.galliumpowered.testplugin.listeners;

import org.apache.logging.log4j.Logger;
import org.galliumpowered.annotation.EventListener;
import org.galliumpowered.event.player.PlayerPlaceBlockEvent;

public class BlockPlaceListener {
    private Logger logger;

    public BlockPlaceListener(Logger logger) {
        this.logger = logger;
    }

    @EventListener
    public void onBlockPlace(PlayerPlaceBlockEvent event) {
        logger.info("Player {} placed block {} in dimension {} at {} {} {} ",
                event.getPlayer().getName(),
                event.getBlock().getId(),
                event.getBlock().getWorld().getDimension(),
                event.getBlock().getX(),
                event.getBlock().getY(),
                event.getBlock().getZ()
        );
    }
}
