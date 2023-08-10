package org.galliumpowered.event;

import org.galliumpowered.Gallium;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

public class EventDispatcherImpl implements EventDispatcher {
    private static final Logger log = LogManager.getLogger();
    EventManager eventManager = Gallium.getEventManager();

    @Override
    public void callEvent(Event event) {
        for (MListener listener : eventManager.listeners.get(event.getClass())) {
            Method method = listener.getMethod();
            System.out.println("Invoking!");
            try {
                method.invoke(listener.getCaller(), event);
            } catch (Exception e) {
                log.error("Error occurred in event " + event.getClass().getSimpleName(), e);
            }
        }
    }
}
