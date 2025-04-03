package org.galliumpowered;

import org.galliumpowered.world.entity.Player;
import org.galliumpowered.world.entity.PlayerImpl;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class ServerImpl implements Server {

    @Override
    public int getCurrentPlayerCount() {
        return Mod.getMinecraftServer().getPlayerCount();
    }

    @Override
    public int getMaxPlayerCount() {
        return Mod.getMinecraftServer().getMaxPlayers();
    }

    @Override
    public ArrayList<Player> getOnlinePlayers() {
        ArrayList<Player> players = new ArrayList<>();
        Mod.getMinecraftServer().getPlayerList().getPlayers().forEach(serverPlayer -> {
            players.add(new PlayerImpl(serverPlayer));
        });

        return players;
    }

    @Override
    public Optional<Player> getPlayerByName(String name) {
        return Gallium.getBridge().getPlayerByName(name);
    }

    @Override
    public Optional<Player> getPlayerByUUID(UUID uuid) {
        return Gallium.getBridge().getPlayerByUUID(uuid);
    }

}
