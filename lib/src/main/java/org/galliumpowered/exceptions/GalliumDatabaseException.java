package org.galliumpowered.exceptions;

public class GalliumDatabaseException extends RuntimeException {
    public GalliumDatabaseException(Throwable t) {
        super(t);
    }

    public GalliumDatabaseException() {
        super();
    }

    public GalliumDatabaseException(String msg) {
        super(msg);
    }
}
