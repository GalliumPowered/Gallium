package org.galliumpowered.command.args;

/**
 * Command argument type
 */
public enum ArgumentType {
    /**
     * Takes up the remainder of the command
     * For example, /mycommand subcommand this will be taken by greedy
     */
    GREEDY,

    /**
     * It is a single word.
     */
    SINGLE,

    /**
     * It is surrounded in quotes
     * For example, /mycommand "hello there" "hello again"
     */
    QUOTED
}
