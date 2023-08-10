package org.galliumpowered.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A command
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * Parent of the command. Used for subcommands
     * @return Command parent
     */
    String parent() default "";
    /**
     * Command aliases
     */
    String[] aliases();

    /**
     * Command description
     */
    String description() default "(no command description)";

    /**
     * Required permissions
     * TODO-ish
     */
    String neededPerms() default "NONE";

    Args[] args() default {};
}
