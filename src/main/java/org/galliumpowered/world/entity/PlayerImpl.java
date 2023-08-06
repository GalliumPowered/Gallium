package org.galliumpowered.world.entity;

import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.galliumpowered.Gamemode;
import org.galliumpowered.world.World;
import org.galliumpowered.world.WorldImpl;
import org.galliumpowered.world.entity.Player;
import org.galliumpowered.util.TextTransformer;

import javax.annotation.Nullable;
import java.util.Optional;

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
    public Optional<Player> getPlayer() {
        return Optional.of(this);
    }

    @Override
    public void sendMessage(Component component) {
        serverPlayer.sendMessage(TextTransformer.toMinecraft(component), Util.NIL_UUID);
    }

    @Override
    public void sendMessage(String message) {
        this.sendMessage(Component.text(message));
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
    public World getWorld() {
        return new WorldImpl(serverPlayer.level);
    }

    @Override
    public String getName() {
        return serverPlayer.getName().getContents().strip();
    }
}
