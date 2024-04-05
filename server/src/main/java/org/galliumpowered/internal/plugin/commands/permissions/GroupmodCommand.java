package org.galliumpowered.internal.plugin.commands.permissions;

import net.kyori.adventure.text.Component;
import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.Command;
import org.galliumpowered.chat.Colors;
import org.galliumpowered.command.CommandCaller;
import org.galliumpowered.command.CommandContext;
import org.galliumpowered.permission.Group;
import org.galliumpowered.permission.PermissionNode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringJoiner;

public class GroupmodCommand {
    @Command(aliases = {"groupmod"}, description = "Modify a group", neededPerms = "PERMSYS")
    public void groupmodCommand(CommandContext ctx) throws SQLException {
        String[] args = ctx.getCommandArgs();
        CommandCaller caller = ctx.getCaller();
        if (args.length == 1) {
            sendUsage(caller);
        } else {
            String groupName = args[1];
            if (args.length == 3) {
                // /groupmod <group> create
                if (args[2].equalsIgnoreCase("create")) {
                    Gallium.getGroupManager().createGroup(new Group(groupName, null));
                    caller.sendMessage(Component.text(Colors.GREEN + "Created group " + Colors.WHITE + groupName));
                } else if (args[2].equalsIgnoreCase("permission") || args[2].equalsIgnoreCase("prefix")) {
                    sendUsage(caller);
                }
                return;
            }
            for (Group group : Gallium.getGroupManager().getGroups()) {
                if (group.getName().equalsIgnoreCase(groupName)) {
                    // correct group
                    if (args.length == 2) {
                        // Display group info
                        caller.sendMessage(Component.text(Colors.GREEN + "--- Group info ---"));
                        caller.sendMessage(Component.text(group.getName()));
                        StringJoiner joiner = new StringJoiner(", ");
                        for (PermissionNode permission : group.getPermissions()) {
                            joiner.add(permission.getPath());
                        }
                        if (joiner.length() == 0) {
                            caller.sendMessage(Component.text("Permissions: No permissions"));
                        } else {
                            caller.sendMessage(Component.text("Permissions: " + joiner));
                        }
                        caller.sendMessage(Component.text("Prefix: " + group.getPrefix()));
                        ctx.getCaller().sendMessage(Component.text(Colors.GREEN + "--------------------"));
                    } else {
                        if (args.length == 4) {
                            // /groupmod <group> permission <permission>
                            if (args[2].equalsIgnoreCase("permission")) {
                                String permission = args[3].toUpperCase();
                                if (group.hasPermission(permission)) {
                                    group.removePermission(permission);
                                    caller.sendMessage(Component.text(Colors.GREEN + "Removed permission " + Colors.WHITE + permission + Colors.GREEN + " from " + Colors.WHITE + groupName));
                                } else {
                                    group.addPermission(permission);
                                    caller.sendMessage(Component.text(Colors.GREEN + "Added permission " + Colors.WHITE + permission + Colors.GREEN + " to " + Colors.WHITE + groupName));
                                }
                            } else if (args[2].equalsIgnoreCase("prefix")) {
                                String prefix = args[3].replace("&", "§");
                                group.setPrefix(prefix);
                                caller.sendMessage(Component.text(Colors.GREEN + "Set group " + Colors.WHITE + groupName + Colors.GREEN + " prefix to " + prefix));
                            } else {
                                sendUsage(caller);
                            }
                        }
                    }
                    return;
                }
            }
            caller.sendMessage(Component.text(Colors.LIGHT_RED + "Could not find that group!"));
        }
    }

    private void sendUsage(CommandCaller caller) {
        caller.sendMessage(Component.text(Colors.LIGHT_RED + "/groupmod <group> [<create|permission|prefix] <permission|prefix>]"));
    }
}
