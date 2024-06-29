package org.galliumpowered.event.player;

import org.galliumpowered.event.CancelableEvent;
import org.galliumpowered.world.entity.Player;

public class PlayerEvent extends CancelableEvent {
    Player player;
    /**
     * The player
     */
    public PlayerEvent(Player player) {
        this.player = player;
    }

    /**
     * The player calling the event
     * @return Player calling the event
     */
    public Player getPlayer() {
        return player;
    }
}
