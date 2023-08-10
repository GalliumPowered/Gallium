package org.galliumpowered.event.player;

import org.galliumpowered.world.entity.Player;

public class PlayerChatEvent extends PlayerEvent {
    private String content;
    /**
     * {@inheritDoc}
     */
    public PlayerChatEvent(Player player, String content) {
        super(player);
        this.content = content;
    }

    /**
     * Get the content of the message
     * @return Message content
     */
    public String getMessage() {
        return this.content;
    }
}
