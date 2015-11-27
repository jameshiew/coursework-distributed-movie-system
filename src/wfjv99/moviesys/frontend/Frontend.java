package wfjv99.moviesys.frontend;

import wfjv99.moviesys.PassiveReplica;
import wfjv99.util.Propagator;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.logging.Logger;

/**
 * Publish an instance of this class to a registry under the name "frontend".
 *
 * Object via which a client submits get() queries and set() updates.
 */
public class Frontend<K extends Serializable, V extends Serializable> extends Propagator<K, V> {

    private static final Logger log = Logger.getLogger(Frontend.class.getName());

    /**
     * Set the primary for this frontend.
     *
     * @param primary the replica that is now the primary, or null if no replica available.
     */
    public void setPrimary(PassiveReplica<K, V> primary) {
        this.unsetPrimary();
        if (primary == null) {
            log.info("Notified that there is no primary.");
        } else {
            log.info("Primary is being updated to " + primary);
            this.addSource(primary);
            this.addStore(primary);
        }
    }

    /**
     * When a primary goes down or changes, this is called.
     */
    private void unsetPrimary() {
        this.clearSources();
        this.clearStores();
    }

    @Override
    public V get(K k) throws RemoteException {
        log.info("Received a get for " + k);
        if (!(this.hasSource() && this.hasStore())) {
            // if no primary, tell client of this
            throw new ServerException("There is currently no primary available. Please try again later.");
        }
        try {
            return super.get(k);
        } catch (RemoteException e) {
            throw new ServerException("Having problems connecting to the primary server. Please try again soon.");
        }
    }

    @Override
    public void set(K k, V v) throws RemoteException {
        if (!(this.hasSource() && this.hasStore())) {
            // if no primary, tell client of this
            throw new ServerException("There is currently no primary available. Please try again later.");
        }
        log.info("Received a set for " + k + ", " + v);
        super.set(k, v);
    }
}
