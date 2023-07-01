package net.zenoc.gallium;

import net.zenoc.gallium.world.entity.Player;
import net.zenoc.gallium.api.world.entity.player.PlayerImpl;

import java.util.ArrayList;

public class ServerImpl implements Server {

    @Override
    public int currentPlayerCount() {
        return Mod.getMinecraftServer().getPlayerCount();
    }

    @Override
    public int maxPlayerCount() {
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
}
