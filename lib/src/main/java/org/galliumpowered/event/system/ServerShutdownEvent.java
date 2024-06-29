package org.galliumpowered.event.system;

import java.util.Optional;

public class ServerShutdownEvent extends SystemEvent {
    String reason;

    /**
     * A server shutdown event
     * @param reason
     */
    public ServerShutdownEvent(String reason) {
        this.reason = reason;
    }

    /**
     * The reason the server gave for shutting down, if any
     * @return The reason the server gave for shutting down, if any
     */
    public Optional<String> getReason() {
        return Optional.of(reason);
    }
}
