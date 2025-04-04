package org.galliumpowered.world.entity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.galliumpowered.Gallium;
import org.galliumpowered.Gamemode;
import org.galliumpowered.Mod;
import org.galliumpowered.pagination.PaginationList;
import org.galliumpowered.pagination.PaginationUtils;
import org.galliumpowered.util.Position;
import org.galliumpowered.world.World;
import org.galliumpowered.world.WorldImpl;
import org.galliumpowered.util.TextTransformer;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class PlayerImpl implements Player {
    private static final int MAX_WIDTH = 320;
    private static final int PAGE_SIZE_LIMIT = 18;
    private final ServerPlayer serverPlayer;

    public PlayerImpl(ServerPlayer serverPlayer) {
        this.serverPlayer = serverPlayer;
    }

    @Override
    public UUID getUUID() {
        return serverPlayer.getUUID();
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
    public void setPosition(Position position) {
        serverPlayer.teleportTo(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public Position getPosition() {
        return Position.of(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
    }

    @Override
    public void setGamemode(Gamemode gamemode) {
        serverPlayer.setGameMode(GameType.byId(gamemode.getId()));
    }

    @Override
    public void kill() {
        serverPlayer.kill();
    }

    @Override
    public World getWorld() {
        return new WorldImpl(serverPlayer.level);
    }

    @Override
    public boolean isOnline() {
        return Mod.getMinecraftServer().getPlayerList().getPlayers().stream()
                .anyMatch(player -> player.getUUID().equals(getUUID()));
    }

    @Override
    public boolean isOperator() {
        return Mod.getMinecraftServer().getPlayerList().isOp(serverPlayer.getGameProfile());
    }

    @Override
    public String getName() {
        return serverPlayer.getName().getContents().strip();
    }

    @Override
    public void sendPaginationList(PaginationList paginationList) {
        sendMessage(PaginationUtils.generateTitle(paginationList, MAX_WIDTH)
                .appendNewline()
                .append(Component.join(JoinConfiguration.newlines(),
                        paginationList.getContents().subList(0,
                                Math.min(paginationList.getContents().size(), getMaxPaginationLines()))))
                .appendNewline()
                .append(PaginationUtils.generateBottom(paginationList.getPadding(), MAX_WIDTH)));

        Gallium.getPaginationManager().submit(this, paginationList);
    }

    @Override
    public int getMaxPaginationLines() {
        return PAGE_SIZE_LIMIT;
    }

    @Override
    public String getPaginationIdentifier() {
        return getUUID().toString();
    }
}
