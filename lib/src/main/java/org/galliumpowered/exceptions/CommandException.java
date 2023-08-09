package org.galliumpowered.exceptions;

public class CommandException extends RuntimeException {
    public CommandException() {
        super();
    }

    public CommandException(Throwable t) {
        super(t);
    }

    public CommandException(String msg) {
        super(msg);
    }
}
