package org.galliumpowered.bridge;

import org.galliumpowered.annotation.Args;
import org.galliumpowered.world.entity.Player;

import java.util.Optional;

/**
 * For bridging NMS to lib. For internal use.
 */
public interface Bridge {

    /**
     * Regusters a command to the server
     * @param alias The alias for the command (i.e. /example)
     * @param permission The permission required to execute the command (i.e. "EXAMPLE)
     */
    void registerCommand(String alias, String permission);

    /**
     *
     * Regusters a command to the server
     * @param alias The alias for the command (i.e. /example)
     * @param permission The permission required to execute the command (i.e. "EXAMPLE)
     * @param args Arguments for the command which may be required
     */
    void registerCommand(String alias, String permission, Args[] args);

    /**
     * Get a {@link Player} by their username
     * @param name The username of the player
     * @return Player (or Optional.empty() if they are not found)
     */
    Optional<Player> getPlayerByName(String name);

    /**
     * Version of Minecraft the server is running.
     * @return Minecraft version
     */
    String getServerVersion();

    /**
     * FOR INTERNAL USE ONLY
     * Loads the internal plugins (id: gallium)
     */
    void loadInternalPlugin();

    /**
     * FOR INTERNAL USE ONLY
     * Loads the test plugin
     */
    void loadTestPlugin();
}
