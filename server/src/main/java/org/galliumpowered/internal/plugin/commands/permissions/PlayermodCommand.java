package org.galliumpowered.internal.plugin.commands.permissions;

import net.kyori.adventure.text.Component;
import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.Command;
import org.galliumpowered.chat.Colors;
import org.galliumpowered.command.CommandCaller;
import org.galliumpowered.command.CommandContext;
import org.galliumpowered.exceptions.GalliumDatabaseException;
import org.galliumpowered.permission.Group;

import java.sql.SQLException;
import java.util.StringJoiner;

public class PlayermodCommand {
    @Command(aliases = {"playermod"}, description = "Modify a player's permissions and groups", permission = "PERMSYS")
    public void playerModCommand(CommandContext ctx) {
        CommandCaller caller = ctx.getCaller();
        String[] args = ctx.getCommandArgs();
        // /playermod
        if (args.length == 1) {
            sendUsage(caller);
            return;
        }
        Gallium.getBridge().getPlayerByName(args[1]).ifPresentOrElse(player -> {
            // /playermod <player>
            if (args.length == 2) {
                caller.sendMessage(Component.text(Colors.GREEN + "--- Player Info ---"));
                caller.sendMessage(Component.text(player.getPrefix() + player.getName()));
                player.getGroup().ifPresentOrElse(group -> {
                    caller.sendMessage(Component.text("Group: " + group.getName()));
                }, () -> {
                    caller.sendMessage(Component.text("Group: Not in a group"));
                });
                StringJoiner joiner = new StringJoiner(", ");
                for (String permission : player.getPermissions()) {
                    joiner.add(permission);
                }
                if (joiner.length() == 0) {
                    caller.sendMessage(Component.text("Permissions: No permissions"));
                } else {
                    caller.sendMessage(Component.text("Permissions: " + joiner));
                }

                ctx.getCaller().sendMessage(Component.text(Colors.GREEN + "--------------------"));
            } else if (args.length == 3) {
                if (args[2].equalsIgnoreCase("ungroup")) {
                    try {
                        player.ungroup();
                        caller.sendMessage(Component.text(Colors.GREEN + "Removed group from " + Colors.WHITE + player.getName()));
                    } catch (SQLException e) {
                        throw new GalliumDatabaseException(e);
                    }
                } else {
                    sendUsage(caller);
                }
            } else {
                if (args[2].equalsIgnoreCase("group")) {
                    String groupName = args[3];
                    try {
                        for (Group group : Gallium.getGroupManager().getGroups()) {
                            if (group.getName().equalsIgnoreCase(groupName)) {
                                player.setGroup(group);
                                caller.sendMessage(Component.text(Colors.GREEN + "Set " + Colors.WHITE + player.getName() + Colors.GREEN + " group to " + Colors.WHITE + group.getName()));
                                return;
                            }
                            caller.sendMessage(Component.text(Colors.LIGHT_RED + "Could not find that group!"));
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else if (args[2].equalsIgnoreCase("permission")) {
                    String permission = args[3].toUpperCase();
                    if (player.hasPermission(permission)) {
                        try {
                            player.removePermission(permission);
                            caller.sendMessage(Component.text(Colors.GREEN + "Removed permission " + Colors.WHITE + permission + Colors.GREEN + " from " + Colors.WHITE + player.getName()));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        try {
                            player.addPermission(permission);
                            caller.sendMessage(Component.text(Colors.GREEN + "Added permission " + Colors.WHITE + permission + Colors.GREEN + " to " + Colors.WHITE + player.getName()));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else if (args[2].equalsIgnoreCase("prefix")) {
                    String prefix = args[3].replace("&", "§");
                    try {
                        player.setPrefix(prefix);
                        caller.sendMessage(Component.text(Colors.GREEN + "Set " + Colors.WHITE + player.getName() + Colors.GREEN + "'s prefix to " + prefix));
                    } catch (SQLException e) {
                        throw new GalliumDatabaseException(e);
                    }
                } else {
                    sendUsage(caller);
                }
            }
        }, () -> caller.sendMessage(Component.text(Colors.LIGHT_RED + "Could not find that player")));
    }

    private void sendUsage(CommandCaller caller) {
        caller.sendMessage(Component.text(Colors.LIGHT_RED + "/playermod <player> [<group|ungroup|permission|prefix> <group|permission>]"));
    }
}
