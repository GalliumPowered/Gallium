package org.galliumpowered.commandsys.args;

import com.mojang.brigadier.arguments.StringArgumentType;

public class ArgsTypeTranslator {
    public static StringArgumentType getAsMinecraft(ArgumentType type) {
        return switch (type) {
            case SINGLE -> StringArgumentType.word();
            case GREEDY -> StringArgumentType.greedyString();
            case QUOTED -> StringArgumentType.string();
        };
    }
}
