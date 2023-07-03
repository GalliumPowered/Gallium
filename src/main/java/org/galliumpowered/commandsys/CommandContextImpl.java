package org.galliumpowered.commandsys;

import org.galliumpowered.world.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public class CommandContextImpl implements CommandContext {

    CommandCaller caller;
    String[] args;
    com.mojang.brigadier.context.CommandContext<?> vanillaCtx;

    public CommandContextImpl(@NonNull CommandCaller caller, @Nullable String[] args, com.mojang.brigadier.context.CommandContext<?> vanillaCtx) {
        this.caller = caller;
        this.args = args;
        this.vanillaCtx = vanillaCtx;
    }

    @Override
    public CommandCaller getCaller() {
        return caller;
    }

    @Override
    public String[] getCommandArgs() {
        return args;
    }

    @Override
    public CommandContext ifPlayer(Consumer<Player> consumer) {
        getCaller().getPlayer().ifPresent(consumer);
        return this;
    }

    @Override
    public CommandContext ifConsole(Consumer<CommandCaller> consumer) {
        if (getCaller().getPlayer().isEmpty()) {
            consumer.accept(getCaller());
        }
        return this;
    }

    @Override
    public Optional<String> getArgument(String name) {
        String result;
        try {
            result = vanillaCtx.getArgument(name, String.class);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        return Optional.of(result);
    }
}
