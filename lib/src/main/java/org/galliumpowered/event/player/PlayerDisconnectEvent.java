package org.galliumpowered.event.player;

import org.galliumpowered.event.Event;
import org.galliumpowered.world.entity.Player;

/**
 * A player disconnect event
 */
public class PlayerDisconnectEvent extends Event {
    boolean suppressed = false;
    Player player;

    /**
     * The player
     * @param player the player
     */
    public PlayerDisconnectEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
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
