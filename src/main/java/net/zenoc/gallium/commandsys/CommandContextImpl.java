package net.zenoc.gallium.commandsys;

import net.zenoc.gallium.world.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CommandContextImpl implements CommandContext {

    CommandCaller caller;
    String[] args;

    public CommandContextImpl(@NonNull CommandCaller caller, @Nullable String[] args) {
        this.caller = caller;
        this.args = args;
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
}
