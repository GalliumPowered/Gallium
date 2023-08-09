package org.galliumpowered.bridge;

import org.galliumpowered.annotation.Args;
import org.galliumpowered.world.entity.Player;

import java.util.Optional;

/**
 * For bridging NMS to lib
 */
public interface Bridge {
    void registerCommand(String alias, String permission);

    void registerCommand(String alias, String permission, Args[] args);

    Optional<Player> getPlayerByName(String name);

    String getServerVersion();

    void loadInternalPlugin();
}
