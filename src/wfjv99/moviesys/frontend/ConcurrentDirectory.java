package wfjv99.moviesys.frontend;

import java.rmi.Remote;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Directory for which only one method may be running at any one time. So registering and unregistering from this directory should be thread-safe.
 * @param <R> the type of remote this is a directory of
 */
public class ConcurrentDirectory<R extends Remote> implements Directory<R> {

    private final Logger log = Logger.getLogger(ConcurrentDirectory.class.getName());
    /**
     * IMPORTANT: store stubs of R rather than instances of R themselves as the stub of R != R.
     */
    private final Set<R> registered;

    public ConcurrentDirectory() {
        log.info("New directory being instantiated...");
        this.registered = new HashSet<>(3);
    }

    @Override
    public void register(R r) {
        synchronized (this) {
            log.info("Registering remote " + r);
            this.registered.add(r);
        }
    }

    @Override
    public void unregister(R r) {
        synchronized (this) {
            log.info("Removing remote " + r);
            this.registered.remove(r);
        }
    }

    @Override
    public Set<R> getRegistered() {
        synchronized (this) {
            return new HashSet<>(this.registered);
        }
    }

    @Override
    public String toString() {
        synchronized (this) {
            return "DirectoryImpl{" +
                    "registered=" + registered.size() +
                    '}';
        }
    }
}
