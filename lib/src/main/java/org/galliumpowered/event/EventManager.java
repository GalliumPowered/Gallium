package org.galliumpowered.event;

import com.google.common.collect.ArrayListMultimap;
import org.galliumpowered.annotation.EventListener;

import java.util.Arrays;

public class EventManager {
    public ArrayListMultimap<Class<? extends Event>, MListener> listeners = ArrayListMultimap.create();

    /**
     * Register a listener to the server
     * @param listener The listener to register. Should be a method.
     */
    public void registerListener(Object listener) {
        Arrays.stream(listener.getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(EventListener.class))
                .forEach(method -> {
                    // Is a valid event listener
                    Class<?> hookClass = method.getParameterTypes()[0];
                    internalRegister(hookClass, new MListener(method.getAnnotation(EventListener.class), listener, method));
                });
    }

    private void internalRegister(Class<?> hookClass, MListener listener) {
        listeners.put((Class<? extends Event>) hookClass, listener);
    }

}
