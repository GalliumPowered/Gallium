package net.zenoc.gallium.api.world.entity.player;

import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.zenoc.gallium.api.Gamemode;
import net.zenoc.gallium.api.chat.ChatMessage;
import net.zenoc.gallium.api.world.entity.Player;
import net.zenoc.gallium.util.TextTransformer;

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
    public void sendMessage(ChatMessage chatMessage) {
        serverPlayer.sendMessage(TextTransformer.toMinecraft(Component.text(chatMessage.getContent())), Util.NIL_UUID);
    }

    @Override
    public void disconnect(@Nullable ChatMessage chatMessage) {
        net.minecraft.network.chat.Component component = TextTransformer.toMinecraft(Component.text(chatMessage.getContent()));
        if (chatMessage == null) {
            serverPlayer.disconnect();
        } else {
            serverPlayer.connection.disconnect(component);
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
