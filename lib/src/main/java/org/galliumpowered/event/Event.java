package org.galliumpowered.event;

import org.galliumpowered.Gallium;

public abstract class Event {
    /**
     * Call the event
     * @return this
     */
    public Event call() {
        Gallium.getEventDispatcher().callEvent(this);
        return this;
    }
}
