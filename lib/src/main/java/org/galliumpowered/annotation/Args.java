package org.galliumpowered.annotation;

import org.galliumpowered.command.args.ArgumentType;

/**
 * Command arguments
 */
public @interface Args {
    /**
     * The {@link ArgumentType} of the arg
     * @return Argument type
     */
    ArgumentType type();

    /**
     * Name/Id of the argument
     * @return Argument name
     */
    String name();
}
