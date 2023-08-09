package org.galliumpowered.command;

import org.galliumpowered.command.console.ConsoleCommandCaller;
import org.galliumpowered.world.entity.Player;

import java.util.Optional;
import java.util.function.Consumer;

public interface CommandContext {
    /**
     * Get the command caller
     */
    CommandCaller getCaller();

    /**
     * Command arguments
     *
     * @return Command arguments
     */
    String[] getCommandArgs();
    CommandContext ifPlayer(Consumer<Player> consumer);

    CommandContext ifConsole(Consumer<ConsoleCommandCaller> consumer);

    Optional<String> getArgument(String name);
}
