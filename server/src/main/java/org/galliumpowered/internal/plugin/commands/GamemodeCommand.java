package org.galliumpowered.internal.plugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.galliumpowered.Gallium;
import org.galliumpowered.Gamemode;
import org.galliumpowered.annotation.Command;
import org.galliumpowered.chat.Colors;
import org.galliumpowered.internal.plugin.GalliumPlugin;
import org.galliumpowered.world.entity.Player;
import org.galliumpowered.command.CommandContext;
import org.galliumpowered.util.NumberUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class GamemodeCommand {
    private static final Component USAGE = Component.text("/gamemode <survival|creative|adventure|spectator|0|1|2|3|s|c|a|sp> [player]").color(NamedTextColor.RED);

    @Command(aliases = { "gamemode", "gm" }, description = "Change a gamemode", neededPerms = "GAMEMODE")
    public void gamemodeCommand(CommandContext ctx) {
        Gamemode gamemode = Gamemode.SURVIVAL;
        Player target = null;
        Player caller = null;

        if (ctx.getCaller().getPlayer().isPresent()) {
            caller = ctx.getCaller().getPlayer().get();

            if (ctx.getCommandArgs().length == 1) {
                ctx.getCaller().sendMessage(USAGE);
                return;
            }

            if (ctx.getCommandArgs().length == 2) {
                target = caller;
            }
        } else {
            // Console or something else
            if (ctx.getCommandArgs().length < 3) {
                ctx.getCaller().sendMessage(USAGE);
                return;
            }
        }

        if (target == null) {
            String targetName = ctx.getCommandArgs()[2];
            Optional<Player> targetOptional = Gallium.getServer().getPlayerByName(targetName);

            if (targetOptional.isPresent()) {
                target = targetOptional.get();
            } else {
                ctx.getCaller().sendMessage(Component.text("Could not find that player!").color(NamedTextColor.RED));
                return;
            }
        }

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

        target.setGamemode(gamemode);
        Component result = Component.text("Gamemode updated to ").color(GalliumPlugin.THEME)
                        .append(Component.text(gamemode.name()).color(NamedTextColor.WHITE));

        if (target != caller) {
            result = result.append(Component.text(target.getName()));
        }

        ctx.getCaller().sendMessage(result);

    }
}
