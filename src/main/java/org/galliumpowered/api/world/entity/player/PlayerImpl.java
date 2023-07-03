package org.galliumpowered.api.world.entity.player;

import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.galliumpowered.api.Gamemode;
import org.galliumpowered.world.entity.Player;
import org.galliumpowered.util.TextTransformer;

import javax.annotation.Nullable;

public class PlayerImpl implements Player {
    ServerPlayer serverPlayer;
    public PlayerImpl(ServerPlayer serverPlayer) {
        this.serverPlayer = serverPlayer;
    }
    @Override
    public String getUUID() {
        return serverPlayer.getUUID().toString().strip();
    }

    @Override
    public void sendMessage(Component component) {
        serverPlayer.sendMessage(TextTransformer.toMinecraft(component), Util.NIL_UUID);
    }

    @Override
    public void disconnect(@Nullable Component component) {
        net.minecraft.network.chat.Component mc = TextTransformer.toMinecraft(component);
        if (component == null) {
            serverPlayer.disconnect();
        } else {
            serverPlayer.connection.disconnect(mc);
        }
    }

    @Override
    public void disconnect() {
        this.disconnect(null);
    }

    @Override
    public void teleport(double x, double y, double z) {
        serverPlayer.teleportTo(x, y, z);
    }

    @Override
    public void setGamemode(Gamemode gamemode) {
        serverPlayer.setGameMode(GameType.byId(gamemode.getId()));
    }

    @Override
    public String getName() {
        return serverPlayer.getName().getContents().strip();
    }
}
