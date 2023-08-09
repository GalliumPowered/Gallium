package org.galliumpowered.command;

import net.kyori.adventure.text.Component;
import org.galliumpowered.world.entity.Player;

import java.util.Optional;

public interface CommandCaller {

    /**
     * An optional of a player. Returns a {@link Player} optional if present.
     * @return Player optional
     */
    Optional<Player> getPlayer();

    /**
     * Send a message to the command caller
     * @param component Message to send
     */
    void sendMessage(Component component);

    /**
     * Send a message to the command caller
     * @param message Message to send
     */
    void sendMessage(String message);
}
