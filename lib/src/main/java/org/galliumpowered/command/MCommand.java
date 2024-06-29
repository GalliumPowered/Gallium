package org.galliumpowered.command;

import org.galliumpowered.Gallium;
import org.galliumpowered.annotation.Command;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Command metadata
 */
public class MCommand {
    Command command;
    Object clazz;
    Method method;

    public MCommand(Command command, Object clazz, Method method) {
        this.command = command;
        this.clazz = clazz;
        this.method = method;
    }

    /**
     * Get the command caller
     */
    public Object getCaller() {
        return clazz;
    }

    /**
     * Get command
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Get method
     */
    public Method getMethod() {
        return method;
    }

    public List<String> suggest(CommandContext ctx) {
        List<String> subcmdNames = new ArrayList<>();
        Gallium.getCommandManager().getSubcommands().forEach((parent, sub) -> {
            if (parent == this) {
                AtomicBoolean canExec = new AtomicBoolean(false);
                ctx.ifConsole(caller -> canExec.set(true));
                ctx.ifPlayer(player -> canExec.set(player.hasPermission(sub.getCommand().neededPerms())));
                if (canExec.get()) {
                    subcmdNames.addAll(Arrays.asList(sub.getCommand().aliases()));
                }
            }
        });
        return subcmdNames;
    }
}
