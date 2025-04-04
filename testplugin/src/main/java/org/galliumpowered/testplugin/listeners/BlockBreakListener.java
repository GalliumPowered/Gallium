package org.galliumpowered.testplugin.listeners;

import org.apache.logging.log4j.Logger;
import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.EventListener;
import org.galliumpowered.event.player.PlayerBreakBlockEvent;

public class BlockBreakListener {
    private Logger logger;

    public BlockBreakListener(Logger logger) {
        this.logger = logger;
    }

    @EventListener
    public void onBlockBreak(PlayerBreakBlockEvent event) {
        logger.info("(DEBUG) Player {} broke block {} in dimension {} at {} {} {} ",
                event.getPlayer().getName(),
                event.getBlock().getId(),
                event.getBlock().getWorld().getDimension(),
                event.getBlock().getX(),
                event.getBlock().getY(),
                event.getBlock().getZ()
        );
    }
}
