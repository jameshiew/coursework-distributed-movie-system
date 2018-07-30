package wfjv99.util;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * When gotten from, returns an upstream result.
 * When set to, sets to all downstream stores.
* @param <K>
* @param <V>
*/
public abstract class Propagator<K extends Serializable, V extends Serializable> implements RemoteSource<K, V>, RemoteStore<K, V> {

    /**
     * Where to fetch results from.
     */
    private final List<RemoteSource<K, V>> upstreamSources;
    /**
     * Where to push updates to.
     */
    private final Set<RemoteStore<K, V>> downstreamStores;

    protected Propagator() {
        this.upstreamSources = new LinkedList<>();
        this.downstreamStores = new HashSet<>(3);
    }

    protected void addSource(RemoteSource<K, V> source) {
        this.upstreamSources.add(source);
    }

    protected void removeSource(RemoteSource<K, V> source) {
        this.upstreamSources.remove(source);
    }

    protected boolean hasSource(RemoteSource<K, V> source) {
        return this.upstreamSources.contains(source);
    }

    protected boolean hasSource() {
        return !this.upstreamSources.isEmpty();
    }

    protected void clearSources() {
        this.upstreamSources.clear();
    }

    protected void addStore(RemoteStore<K, V> store) {
        this.downstreamStores.add(store);
    }

    protected void removeStore(RemoteStore<K, V> store) {
        this.downstreamStores.remove(store);
    }

    protected boolean hasStore(RemoteStore<K, V> store) {
        return this.downstreamStores.contains(store);
    }

    protected boolean hasStore() {
        return !this.downstreamStores.isEmpty();
    }

    protected void clearStores() {
        this.downstreamStores.clear();
    }

    /**
     * Try to get a result for k. Try each upstream source in order until a
     * non-null result is gotten, or all have been tried.
     * @param k
     * @return the first result retrieved that isn't null, or null if no result could be retrieved.
     * @throws RemoteException
     */
    @Override
    public V get(K k) throws RemoteException {
        V result = null;
        for (int i = 0; (i < this.upstreamSources.size()) && (result == null); i++) {
            result = this.upstreamSources.get(i).get(k);
        }
        if (result != null) {
            this.set(k, result);
        }
        return result;
    }

    /**
     * Propagate the change to all remote stores.
     * @param k
     * @param v
     * @throws RemoteException
     */
    @Override
    public void set(K k, V v) throws RemoteException {
        for (RemoteStore<K, V> remoteStore : this.downstreamStores) {
            remoteStore.set(k, v);
        }
    }
}
