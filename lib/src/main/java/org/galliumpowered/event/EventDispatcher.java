package org.galliumpowered.event;

public interface EventDispatcher {
    /**
     * Call an event
     * @param event
     */
    void callEvent(Event event);
}
