package org.galliumpowered.event;

public abstract class CancelableEvent extends Event {
    public boolean isCancelled;
    /**
     * Cancel the event
     */
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    /**
     * Whether the event was cancelled
     * @return whether the event was cancelled
     */
    public boolean isCancelled() {
        return this.isCancelled;
    }

}
