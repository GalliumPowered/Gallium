package org.galliumpowered;

import net.kyori.adventure.text.Component;
import org.galliumpowered.world.entity.Player;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public interface Server {

    /**
     * Get online players as a number
     *
     * @return number of {@link Player}s online
     */
    int getCurrentPlayerCount();

    /**
     * Get the maximum number of players that can be on the server
     *
     * @return number max {@link Player}s
     */
    int getMaxPlayerCount();

    /**
     * Get all the online players
     * @return ArrayList of the players
     */
    ArrayList<Player> getOnlinePlayers();

    /**
     * Get a player by their name
     *
     * @param name Player name
     * @return The player, if present, otherwise an empty {@link Optional}
     */
    Optional<Player> getPlayerByName(String name);

    /**
     * Get a player by their UUID
     *
     * @param uuid UUID of the player
     * @return Player UUID
     */
    Optional<Player> getPlayerByUUID(UUID uuid);

    /**
     * Send a message to everyone online
     * @param component The message to send
     */
    default void sendMsgToAll(Component component) {
        getOnlinePlayers().forEach(player -> player.sendMessage(component));
    }
}
