package org.galliumpowered.internal.plugin.commands.plugin;

import net.kyori.adventure.text.Component;
import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.Args;
import org.galliumpowered.annotation.Command;
import org.galliumpowered.chat.Colors;
import org.galliumpowered.command.CommandContext;
import org.galliumpowered.command.args.ArgumentType;

public class PluginInfoCommand {
    @Command(aliases = {"plugininfo"}, description = "Show information about a plugin", neededPerms = "PLUGINS", args = @Args(type = ArgumentType.SINGLE, name = "plugin"))
    public void pluginInfoCommand(CommandContext ctx) {
        ctx.getArgument("plugin").ifPresentOrElse(pluginId -> {
            Gallium.getPluginManager().getPluginById(pluginId).ifPresentOrElse(container -> {
                ctx.getCaller().sendMessage(Component.text(Colors.GREEN + "----- Plugin info -----"));
                ctx.getCaller().sendMessage(Component.text("Name: " + container.getMetadata().getName()));
                ctx.getCaller().sendMessage(Component.text("ID: " + container.getMetadata().getId()));
                ctx.getCaller().sendMessage(Component.text("Description: " + container.getMetadata().getDescription()));
                ctx.getCaller().sendMessage(Component.text("Version: " + container.getMetadata().getVersion()));
                ctx.getCaller().sendMessage(Component.text("Authors: " + String.join(", ", container.getMetadata().getAuthors())));
                ctx.getCaller().sendMessage(Component.text("Main Class: " + container.getMetadata().getMainClass()));
                ctx.getCaller().sendMessage(Component.text(Colors.GREEN + "-----------------------"));
            }, () -> {
                ctx.getCaller().sendMessage(Component.text(Colors.LIGHT_RED + "Could not find that plugin!"));
            });
        }, () -> {
            ctx.getCaller().sendMessage(Component.text(Colors.LIGHT_RED + "/plugininfo <plugin id>"));
        });
    }
}
