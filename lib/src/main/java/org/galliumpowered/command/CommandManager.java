package org.galliumpowered.command;

import org.galliumpowered.annotation.Command;
import org.galliumpowered.Gallium;
import org.galliumpowered.plugin.PluginContainer;
import org.galliumpowered.plugin.metadata.PluginMetadata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class CommandManager {
    private HashMap<String, MCommand> commands = new HashMap<>();
    private ConcurrentHashMap<String, PluginMetadata> pluginCommands = new ConcurrentHashMap<>();
    private HashMap<MCommand, MCommand> subcommands = new HashMap<>();

    /**
     * Register a command on the server
     * @param command A class instance containing a {@link Command} annotation.
     * @param plugin A {@link PluginContainer} instance
     */
    public void registerCommand(Object command, PluginContainer plugin) {
        registerCommand(command, plugin.getMetadata());
    }

    /**
     * Register a command on the server
     * @param command A class instance containing a {@link Command} annotation.
     * @param meta A {@link PluginMetadata} instance
     */
    public void registerCommand(Object command, PluginMetadata meta) {
        Arrays.stream(command.getClass().getMethods())
            .filter(method -> method.isAnnotationPresent(Command.class))
            .filter(method -> method.getParameters().length == 1)
            .filter(method -> method.getParameterTypes()[0] == CommandContext.class)
            .map(method -> new MCommand(method.getAnnotation(Command.class), command, method))
            .forEach(cmd -> doRegister(cmd, meta));
    }

    private void doRegister(MCommand cmd, PluginMetadata meta) {
        if (!cmd.getCommand().parent().equals("")) {
            // Subcommand
            subcommands.put(commands.get(cmd.getCommand().parent()), cmd);
        } else {
            for (String alias : cmd.getCommand().aliases()) {
                internalRegister(alias, cmd, meta);

                commands.put(meta.getId() + ":" + alias, cmd);
                commands.put(alias, cmd);

                pluginCommands.put(meta.getId() + ":" + alias, meta);
                pluginCommands.put(alias, meta);
            }
        }
    }

    private void internalRegister(String alias, MCommand cmd, PluginMetadata meta) {
        if (cmd.getCommand().args().length == 0) {
            Gallium.getBridge().registerCommand(alias, cmd.getCommand().neededPerms());
            Gallium.getBridge().registerCommand(meta.getId() + ":" + alias, cmd.getCommand().neededPerms());
        } else {
            // FIXME: Multiple args
            Gallium.getBridge().registerCommand(alias, cmd.getCommand().neededPerms(), cmd.getCommand().args());
            Gallium.getBridge().registerCommand(meta.getId() + ":" + alias, cmd.getCommand().neededPerms(), cmd.getCommand().args());
        }
    }

    public void unregisterCommand(String alias) {
        commands.remove(alias);
        pluginCommands.remove(alias);
    }

    public void unregisterAllPluginCommands(PluginMetadata meta) {
        for (String alias : pluginCommands.keySet()) {
            if (pluginCommands.get(alias) == meta) {
                unregisterCommand(alias);
            }
        }
    }

    public HashMap<String, MCommand> getCommands() {
        return commands;
    }

    public HashMap<MCommand, MCommand> getSubcommands() {
        return subcommands;
    }
}
