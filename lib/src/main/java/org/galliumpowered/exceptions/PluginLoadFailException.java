package org.galliumpowered.exceptions;

public class PluginLoadFailException extends PluginException {
    public PluginLoadFailException() {
        super();
    }
    public PluginLoadFailException(Throwable t) {
        super(t);
    }

    public PluginLoadFailException(String s) {
        super(s);
    }
}
