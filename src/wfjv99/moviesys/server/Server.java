package wfjv99.moviesys.server;

import wfjv99.moviesys.PassiveReplica;
import wfjv99.util.Propagator;
import wfjv99.util.RemoteSource;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Propagates updates to any secondaries registered with it.
 */
public class Server<K extends Serializable, V extends Serializable> extends Propagator<K, V> implements PassiveReplica<K, V> {

    private static final Logger log = Logger.getLogger(Server.class.getName());
    /**
     * First results are tried to be gotten from here, and new results are cached here.
     */
    private final Map<K, V> localKnowledge;

    /**
     * Can only have one remote source.
     * @param remoteSource The sole remote source.
     */
    public Server(RemoteSource<K, V> remoteSource) {
        super();
        log.info("Instantiating new server...");
        this.localKnowledge = new HashMap<K, V>(3);
        this.addSource(remoteSource);
    }

    @Override
    public V get(K k) throws RemoteException {
        log.info("Received get request for " + k);
        V result;
        if (this.localKnowledge.containsKey(k)) {
            result = this.localKnowledge.get(k);
            log.info("Local result for " + k + ": " + result);
        } else {
            result = super.get(k);
            log.info("Remote result for " + k + ": " + result);
            this.set(k, result);
            log.info("Result propagated to other servers.");
        }
        return result;
    }

    @Override
    public void set(K k, V v) throws RemoteException {
        log.info("Received set request for " + k + " -- " + v);
        if (this.localKnowledge.containsKey(k)) {
            log.warning("Overwriting local knowledge for " + k);
        }
        this.localKnowledge.put(k, v);
        log.info("Recorded " + k + " -- " + v + " locally.");
        try {
            super.set(k, v);
        } catch (RemoteException e) {
            log.log(Level.WARNING, "Exception thrown while propagating change!", e);
        }
    }

    @Override
    public void addSecondary(PassiveReplica<K, V> secondary) throws RemoteException {
        if (secondary == null) throw new NullPointerException();
        if (this.hasStore(secondary)) return;
        log.info("Adding secondary: " + secondary);
        this.addStore(secondary);
    }

    @Override
    public void removeSecondary(PassiveReplica<K, V> secondary) throws RemoteException {
        if (secondary == null) throw new NullPointerException();
        if (!this.hasSource(secondary)) return;
        log.info("Removing secondary: " + secondary);
        this.removeStore(secondary);
    }

    @Override
    public void clearSecondaries() throws RemoteException {
        log.info("Removing all secondaries.");
        this.clearStores();
    }

    @Override
    public void ping() throws RemoteException {
        return;
    }
}
