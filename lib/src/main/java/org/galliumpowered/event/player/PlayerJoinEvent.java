package org.galliumpowered.event.player;

import org.galliumpowered.world.entity.Player;

/**
 * A player join event
 */
public class PlayerJoinEvent extends PlayerEvent {
    boolean suppressed = false;
    /**
     * The player
     *
     * @param player The player
     */
    public PlayerJoinEvent(Player player) {
        super(player);
    }

    /**
     * Hide the message
     */
    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
    }

    public boolean isSuppressed() {
        return suppressed;
    }
}
