package org.galliumpowered.internal.plugin.commands.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.Args;
import org.galliumpowered.annotation.Command;
import org.galliumpowered.command.CommandContext;
import org.galliumpowered.command.args.ArgumentType;
import org.galliumpowered.internal.plugin.GalliumPlugin;

public class PluginInfoCommand {
    @Command(aliases = {"plugininfo"}, description = "Show information about a plugin", neededPerms = "PLUGINS", args = @Args(type = ArgumentType.SINGLE, name = "plugin"))
    public void pluginInfoCommand(CommandContext ctx) {
        ctx.getArgument("plugin").ifPresentOrElse(pluginId -> {
            Gallium.getPluginManager().getPluginById(pluginId).ifPresentOrElse(container -> {
                String prefix = "===== Plugin info: ";
                String suffix = " =====";
                Component title = Component.text(prefix).color(GalliumPlugin.THEME)
                        .append(Component.text(container.getMetadata().getName()).color(NamedTextColor.WHITE))
                        .append(Component.text(suffix).color(GalliumPlugin.THEME));

                int length = prefix.length() + container.getMetadata().getName().length() + suffix.length();

                ctx.getCaller().sendMessage(
                    Component.text()
                            .append(title)
                            .append(Component.newline())
                            .append(Component.text("Name: ").color(GalliumPlugin.THEME))
                            .append(Component.text(container.getMetadata().getName()))
                            .append(Component.newline())
                            .append(Component.text("ID: ").color(GalliumPlugin.THEME))
                            .append(Component.text(container.getMetadata().getId()))
                            .append(Component.newline())
                            .append(Component.text("Description: ").color(GalliumPlugin.THEME))
                            .append(Component.text(container.getMetadata().getDescription()))
                            .append(Component.newline())
                            .append(Component.text("Authors: ").color(GalliumPlugin.THEME))
                            .append(Component.text(String.join(", ", container.getMetadata().getAuthors())))
                            .append(Component.newline())
                            .append(Component.text("Main class: ").color(GalliumPlugin.THEME))
                            .append(Component.text(container.getMetadata().getMainClass()))
                            .append(Component.newline())
                            .append(Component.text("=".repeat(length)).color(GalliumPlugin.THEME))
                            .build()
                );
            }, () -> ctx.getCaller().sendMessage(Component.text("Could not find that plugin!").color(NamedTextColor.RED)));
        }, () -> ctx.getCaller().sendMessage(Component.text("/plugininfo <plugin id>").color(NamedTextColor.RED)));
    }
}
