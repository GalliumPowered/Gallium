package org.galliumpowered.testplugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.Args;
import org.galliumpowered.annotation.Command;
import org.galliumpowered.chat.Colors;
import org.galliumpowered.command.CommandContext;
import org.galliumpowered.command.args.ArgumentType;

public class TestCommands {
    @Command(
            aliases = "test",
            description = "A test command, for testing Gallium!",
            args = {
                    @Args(name = "something", type = ArgumentType.SINGLE),
                    @Args(name = "greedy", type = ArgumentType.GREEDY)
            })
    public void testCommand(CommandContext ctx) {
        ctx.getArgument("something").ifPresent(something -> {
            ctx.getCaller().sendMessage(Component.text(something));
        });
        ctx.getArgument("greedy").ifPresent(greedy -> {
            ctx.getCaller().sendMessage(Component.text(greedy));
        });
    }

    @Command(aliases = "test2", description = "More testing", args = @Args(name = "helo", type = ArgumentType.GREEDY))
    public void test2Command(CommandContext ctx) {
        ctx.getArgument("helo").ifPresent(helo -> {
            ctx.getCaller().sendMessage(Component.text(helo));
        });
    }

    @Command(aliases = "test3", description = "More testing", args = @Args(name = "helo", type = ArgumentType.QUOTED))
    public void test3Command(CommandContext ctx) {
        ctx.getArgument("helo").ifPresent(helo -> {
            ctx.getCaller().sendMessage(Component.text(helo));
        });
    }

    @Command(aliases = "test4", description = "More testing", args = {
            @Args(name = "helo", type = ArgumentType.QUOTED),
            @Args(name = "helo1", type = ArgumentType.SINGLE)
    })
    public void test4Command(CommandContext ctx) {
        ctx.getArgument("helo").ifPresent(helo -> {
            ctx.getCaller().sendMessage(Component.text(helo));
        });
        ctx.getArgument("helo1").ifPresent(helo1 -> {
            ctx.getCaller().sendMessage(Component.text(helo1));
        });
    }

    @Command(aliases = "test5", description = "Command without any params")
    public void test5Command() {

    }

    @Command(aliases = "test6", description = "Command with incorrect param types")
    public void test6Command(Gallium gallium) {

    }

    @Command(aliases = "test7", description = "Testing ifPlayer and ifConsole")
    public void test7Command(CommandContext ctx) {
        ctx.ifPlayer(player -> {
            player.sendMessage("Hey, you're a player!");
        }).ifConsole(console -> {
            console.sendMessage("You're the console!");
        });
    }

    @Command(aliases = "test8", description = "Testing player current world")
    public void test8Command(CommandContext ctx) {
        ctx.ifPlayer(player -> {
            player.sendMessage("Current world dimension: " + player.getWorld().getDimension());
            player.sendMessage("Current world difficulty: " + player.getWorld().getDifficulty());
        }).ifConsole(console -> {
            console.sendMessage("You cannot use this command as the console!");
        });
    }

    @Command(aliases = "suicide", description = "it's a way out")
    public void suicideCommand(CommandContext ctx) {
        ctx.ifPlayer(player -> {
            player.kill();
            Gallium.getServer().sendMsgToAll(Component.text(player.getPrefix() + player.getName() + Colors.GREEN + " took the easy way out."));
        });
    }

    @Command(aliases = "amiop", description = "Check if you are an operator")
    public void checkOpCommand(CommandContext ctx) {
        ctx.ifPlayer(player ->
                player.sendMessage(Component.text(player.isOperator()))
        ).ifConsole(console ->
                console.sendMessage("You're the console, silly, of course you're op.")
        );
    }

    @Command(aliases = "searchplayer", description = "Search for a player (debug command)", args = {
            @Args(type = ArgumentType.SINGLE, name = "username")
    })
    public void searchPlayerCommand(CommandContext ctx) {
        ctx.getArgument("username").ifPresentOrElse(username -> {
            // Search for the player by their username
            Gallium.getServer().getPlayerByName(username).ifPresentOrElse(player -> {
                ctx.getCaller().sendMessage(
                        "Found player! Username: " + player.getName() + " UUID: " +
                                player.getUUID());
            }, () -> {
                ctx.getCaller().sendMessage(
                        Component.text("Could not find that player").color(NamedTextColor.RED));
            });

        }, () -> {
            ctx.getCaller()
                    .sendMessage(Component.text("Specify a username!").color(NamedTextColor.RED));
        });
    }
}
