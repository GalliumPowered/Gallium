package org.galliumpowered.internal.plugin.commands;

import net.kyori.adventure.text.Component;
import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.Command;
import org.galliumpowered.chat.Colors;
import org.galliumpowered.command.CommandContext;

public class GalliumCommand {
    @Command(aliases = {"gallium"}, description = "Information about Gallium")
    public void galliumCommand(CommandContext ctx) {
        ctx.getCaller().sendMessage(Component.text(Colors.GREEN + "--- Gallium " + Colors.WHITE + "-" + Colors.GREEN + " Version 1.1.0-beta.3 ---"));
        ctx.getCaller().sendMessage(Component.text(Colors.GREEN + "Developers: " + Colors.WHITE + "SlimeDiamond, TheKodeToad"));
        ctx.getCaller().sendMessage(Component.text(Colors.GREEN + "API version: " + Colors.WHITE + Gallium.getVersion()));
        ctx.getCaller().sendMessage(Component.text(Colors.GREEN + "Minecraft version: " + Colors.WHITE + Gallium.getBridge().getServerVersion()));
        ctx.getCaller().sendMessage(Component.text(Colors.GREEN + "--------------------"));
    }
}
