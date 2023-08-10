package org.galliumpowered.annotation;

import org.galliumpowered.command.args.ArgumentType;

public @interface Args {
    ArgumentType type();
    String name();
}
