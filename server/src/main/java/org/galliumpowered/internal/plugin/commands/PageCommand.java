package org.galliumpowered.internal.plugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.Command;
import org.galliumpowered.command.CommandContext;

public class PageCommand {
    @Command(aliases = "page", description = "Pagination command root")
    public void pageCommand(CommandContext ctx) {
        ctx.getCaller().sendMessage(Component.text("Usage: /page <next|last>").color(NamedTextColor.RED));
    }

    @Command(parent = "page", aliases = "next", description = "Go to the next page")
    public void pageNextCommand(CommandContext ctx) {
        Gallium.getPaginationManager().nextPage(ctx.getCaller());
    }

    @Command(parent = "page", aliases = "prev", description = "Go to the previous page")
    public void pagePrevious(CommandContext ctx) {
        Gallium.getPaginationManager().previousPage(ctx.getCaller());
    }
}
