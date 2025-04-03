package org.galliumpowered.internal.plugin.commands;

import net.kyori.adventure.text.Component;
import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.Command;
import org.galliumpowered.command.CommandContext;
import org.galliumpowered.internal.plugin.GalliumPlugin;

public class GalliumCommand {
    @Command(aliases = {"gallium"}, description = "Information about Gallium")
    public void galliumCommand(CommandContext ctx) {
        ctx.getCaller().sendMessage(
                Component.text()
                        .append(Component.text("========== Gallium ==========").color(GalliumPlugin.THEME))
                        .append(Component.newline())
                        .append(Component.text("Authors: ").color(GalliumPlugin.THEME))
                        .append(Component.text("SlimeDiamond")) // TODO: Pull from elsewhere
                        .append(Component.newline())
                        .append(Component.text("Version: ").color(GalliumPlugin.THEME))
                        .append(Component.text(Gallium.getVersion()))
                        .append(Component.newline())
                        .append(Component.text("Minecraft: ").color(GalliumPlugin.THEME))
                        .append(Component.text(Gallium.getBridge().getServerVersion()))
                        .append(Component.newline())
                        .append(Component.text("=============================").color(GalliumPlugin.THEME))
                        .build()
        );
    }
}
