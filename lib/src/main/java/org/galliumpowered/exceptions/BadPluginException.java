package org.galliumpowered.exceptions;

public class BadPluginException extends PluginException {
    public BadPluginException(String msg) {
        super(msg);
    }

    public BadPluginException(Throwable t) {
        super(t);
    }
}
