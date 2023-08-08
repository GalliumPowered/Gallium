package org.galliumpowered.internal.plugin.commands.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.galliumpowered.Gallium;
import org.galliumpowered.chat.Colors;
import org.galliumpowered.annotation.Command;
import org.galliumpowered.command.CommandContext;
import org.galliumpowered.plugin.metadata.PluginMetadata;

public class PluginListCommand {
    @Command(aliases = {"pluginlist", "plugins"}, description = "Show all plugins loaded on the server", neededPerms = "PLUGINS")
    public void pluginListCommand(CommandContext ctx) {
        ctx.getCaller().sendMessage(Component.text(Colors.GREEN + "--- Plugin list ---"));
//        ctx.getCaller().sendMessage(Component.text(Colors.GREEN + "--------------------"));
        Gallium.getPluginManager().getLoadedPlugins().forEach(plugin -> {
            PluginMetadata meta = plugin.getMetadata();
            ctx.getCaller().sendMessage(
                    Component.text(Colors.WHITE + meta.getName() + " (" + meta.getId() + ")")
                            .hoverEvent(HoverEvent.showText(
                                    Component.text(
                                            "Description: " + meta.getDescription() + "\n\n" +
                                            "Click for more info"
                                    )
                            ))
                            .clickEvent(ClickEvent.runCommand("/plugininfo " + meta.getId()))
            );
        });
        ctx.getCaller().sendMessage(Component.text(Colors.GREEN + "--------------------"));
    }
}
