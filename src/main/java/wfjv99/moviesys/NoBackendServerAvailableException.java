package wfjv99.moviesys;

import java.rmi.ServerException;

public class NoBackendServerAvailableException extends ServerException {

    public NoBackendServerAvailableException() {
        super(null);
    }

    public NoBackendServerAvailableException(String message) {
        super(message);
    }

    public NoBackendServerAvailableException(String message, Exception exception) {
        super(message, exception);
    }
}
