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
        logger.info(event.getBlock().getWorld().getDimension());
    }
}
