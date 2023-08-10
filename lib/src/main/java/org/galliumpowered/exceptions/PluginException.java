package org.galliumpowered.exceptions;

public class PluginException extends RuntimeException {
    public PluginException() {
        super();
    }
    public PluginException(Throwable t) {
        super(t);
    }

    public PluginException(String msg) {
        super(msg);
    }
}
