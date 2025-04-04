package org.galliumpowered.command;

import net.kyori.adventure.audience.Audience;
import org.galliumpowered.pagination.PaginationListAudience;
import org.galliumpowered.world.entity.Player;

import java.util.Optional;

public interface CommandCaller extends Audience, PaginationListAudience {

    /**
     * An optional of a player. Returns a {@link Player} optional if present.
     * @return Player optional
     */
    Optional<Player> getPlayer();

    /**
     * Send a message to the command caller
     * @param message Message to send
     */
    void sendMessage(String message);
}
