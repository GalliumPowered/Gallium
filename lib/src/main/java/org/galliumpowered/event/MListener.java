package org.galliumpowered.event;

import org.galliumpowered.annotation.EventListener;

import java.lang.reflect.Method;

public class MListener {
    EventListener listener;
    Object clazz;
    Method method;

    public MListener(EventListener listener, Object clazz, Method method) {
        this.listener = listener;
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
    public EventListener getListener() {
        return listener;
    }

    /**
     * Get method
     */
    public Method getMethod() {
        return method;
    }
}
