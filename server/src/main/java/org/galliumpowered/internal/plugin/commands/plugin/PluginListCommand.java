package org.galliumpowered.internal.plugin.commands.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.Command;
import org.galliumpowered.command.CommandContext;
import org.galliumpowered.internal.plugin.GalliumPlugin;

public class PluginListCommand {
    @Command(aliases = {"pluginlist", "plugins"}, description = "Show all plugins loaded on the server", permission = "PLUGINS")
    public void pluginListCommand(CommandContext ctx) {
        ctx.getCaller().sendMessage(
                Component.text()
                        .append(Component.text("======= Plugin list =======").color(GalliumPlugin.THEME))
                        .append(Component.newline())
                        .append(Component.join(JoinConfiguration.separator(Component.newline()),
                                Gallium.getPluginManager().getLoadedPlugins().stream().map(plugin ->
                                                Component.text(plugin.getMetadata().getName())
                                                        .hoverEvent(HoverEvent.showText(Component.text(plugin.getMetadata().getId())))
                                                        .clickEvent(ClickEvent.runCommand("/plugininfo " + plugin.getMetadata().getId())))
                                        .toList()
                        ))
                        .append(Component.newline())
                        .append(Component.text("============================").color(GalliumPlugin.THEME))
                        .build()
        );
    }
}
