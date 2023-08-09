package org.galliumpowered.internal.plugin.commands;

import net.kyori.adventure.text.Component;
import org.galliumpowered.Gallium;
import org.galliumpowered.Gamemode;
import org.galliumpowered.annotation.Command;
import org.galliumpowered.chat.Colors;
import org.galliumpowered.world.entity.Player;
import org.galliumpowered.command.CommandContext;
import org.galliumpowered.util.NumberUtils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class GamemodeCommand {
    @Command(aliases = { "gamemode", "gm" }, description = "Change a gamemode", neededPerms = "GAMEMODE")
    public void gamemodeCommand(CommandContext ctx) {
        Gamemode gamemode = Gamemode.SURVIVAL;
        AtomicReference<Player> target = new AtomicReference<>();
        AtomicBoolean shouldContinue = new AtomicBoolean(true);
        ctx.ifPlayer(player -> {
            if (ctx.getCommandArgs().length == 1) {
                ctx.getCaller().sendMessage(Component.text(Colors.LIGHT_RED + "/gamemode <survival|creative|adventure|spectator|0|1|2|3|s|c|a|sp> [player]"));
                shouldContinue.set(false);
                return;
            }
            if (ctx.getCommandArgs().length == 2) {
                target.set(player);
            }
        }).ifConsole(console -> {
            if (ctx.getCommandArgs().length < 3) {
                ctx.getCaller().sendMessage(Component.text(Colors.LIGHT_RED + "/gamemode <survival|creative|adventure|spectator|0|1|2|3|s|c|a|sp> <player>"));
                shouldContinue.set(false);
            }
        });

        if (!shouldContinue.get()) return;

        if (target.get() == null) {
            String targetName = ctx.getCommandArgs()[2];
            Gallium.getBridge().getPlayerByName(targetName).ifPresentOrElse(target::set, () -> {
                ctx.getCaller().sendMessage(Component.text(Colors.LIGHT_RED + "Could not find that player!"));
                shouldContinue.set(false);
            });
        }

        if (!shouldContinue.get()) return;

        if (NumberUtils.isNumeric(ctx.getCommandArgs()[1])) {
            gamemode = Gamemode.fromId(Integer.parseInt(ctx.getCommandArgs()[1]));
        } else {
            gamemode = switch (ctx.getCommandArgs()[1]) {
                case "survival", "s" -> Gamemode.SURVIVAL;
                case "creative", "c" -> Gamemode.CREATIVE;
                case "adventure", "a" -> Gamemode.ADVENTURE;
                case "spectator", "sp" -> Gamemode.SPECTATOR;
                default -> gamemode;
            };
        }

        target.get().setGamemode(gamemode);
    }
}
