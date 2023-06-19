package net.zenoc.gallium.event;

import net.zenoc.gallium.Gallium;
import net.zenoc.gallium.api.event.Event;
import net.zenoc.gallium.eventsys.EventDispatcher;
import net.zenoc.gallium.eventsys.EventManager;
import net.zenoc.gallium.eventsys.MListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.Iterator;

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
