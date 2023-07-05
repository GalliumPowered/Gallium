package org.galliumpowered.command;

import net.minecraft.server.level.ServerPlayer;
import org.galliumpowered.world.entity.Player;
import org.galliumpowered.world.entity.PlayerImpl;

import javax.annotation.Nullable;
import java.util.Optional;

public class CommandCallerImpl implements CommandCaller {
    ServerPlayer serverPlayer;

    public CommandCallerImpl(@Nullable ServerPlayer serverPlayer) {
        this.serverPlayer = serverPlayer;
    }

    @Override
    public Optional<Player> getPlayer() {
        if (serverPlayer == null) {
            return Optional.empty();
        } else {
            return Optional.of(new PlayerImpl(serverPlayer));
        }
    }
}
