package org.galliumpowered.event.system;

import java.util.Optional;

public class ServerShutdownEvent extends SystemEvent {
    String reason;
    public ServerShutdownEvent(String reason) {
        this.reason = reason;
    }

    public Optional<String> getReason() {
        return Optional.of(reason);
    }
}
