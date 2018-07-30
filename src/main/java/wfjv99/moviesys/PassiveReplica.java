package wfjv99.moviesys;

import wfjv99.util.Pingable;
import wfjv99.util.RemoteSource;
import wfjv99.util.RemoteStore;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Represents a server in a passive replication system.
 *
 * In a distributed system based on the passive replication architecture, at any
 * one time there is one primary replica. Frontend(s) send all messages received
 * from clients to this primary replica, which acts on them and ensures that any
 * changes to the 'state' of the distributed system (i.e. the knowledge or database
 * the system holds) are propagated to all other replicas in the system, the secondary
 * replicas.
 */
public interface PassiveReplica<K extends Serializable, V extends Serializable> extends Pingable, RemoteStore<K, V>, RemoteSource<K, V> {

    /**
     * Calling this method implies that this replica is the primary replica.
     *
     * This method may be called when a new replica comes online.
     *
     * @param secondary the replica to be added to this primary replica's repertoire
     *                  of secondary replicas.
     * @throws RemoteException
     */
    void addSecondary(PassiveReplica<K, V> secondary) throws RemoteException;

    /**
     * Calling this method implies that this replica is the primary replica.
     *
     * This method may be called, for example, if a secondary replica goes offline.
     *
     * @param secondary the replica to be removed from this primary replica's repertoire
     *                  of secondary replicas, if this replica was ever present among
     *                  them
     * @throws RemoteException
     */
    void removeSecondary(PassiveReplica<K, V> secondary) throws RemoteException;

    /**
     * Would be called when a replica is relegated from being a primary to a
     * secondary replica.
     * @throws RemoteException
     */
    void clearSecondaries() throws RemoteException;

}
