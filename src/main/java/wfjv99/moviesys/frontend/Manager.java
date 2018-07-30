package wfjv99.moviesys.frontend;

import wfjv99.moviesys.PassiveReplica;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages a directory of passive replicas, ensuring one of them is a primary
 * at all times.
 *
 * Ensures the primary replica knows of all other reachable replicas in the directory.
 *
 * Maintains a set of frontends to which knowledge of the primary replica is passed to.
 */
public class Manager<K extends Serializable, V extends Serializable> implements Runnable {

    private static final Logger log = Logger.getLogger(Manager.class.getName());
    /**
     * The directory representing the system. The manager chooses a replica from
     * this directory to be the primary one.
     */
    private final Directory<PassiveReplica<K, V>> directory;
    /**
     * Frontends that this manager will push knowledge of a new primary to. Must
     * be on the same machine.
     */
    private final Set<Frontend<K, V>> frontends;
    /**
     * How long between each check of the directory to see if it has been updated.
     */
    private final long timeBetweenPollInMillis;
    /**
     * The primary passive replica STUB.
     */
    private PassiveReplica<K, V> primaryStub;
    /**
     * Snapshot of registered replicas from current polling.
     */
    private Set<PassiveReplica<K, V>> currentSnapshot;
    /**
     * Snapshot of the registered replicas from last polling.
     */
    private Set<PassiveReplica<K, V>> previousSnapshot;

    public Manager(final Directory<PassiveReplica<K, V>> directory, long timeBetweenPollInMillis) {
        if (directory == null) throw new NullPointerException();
        if (timeBetweenPollInMillis <= 0) throw new IllegalArgumentException();
        log.info("Instantiating new manager for " + directory);
        this.timeBetweenPollInMillis = timeBetweenPollInMillis;
        this.directory = directory;
        this.frontends = new HashSet<>(1);
        this.currentSnapshot = new HashSet<>(3);
        this.previousSnapshot = new HashSet<>(3);
        this.primaryStub = null;
    }

    /**
     * Add a frontend.
     *
     * @param frontend to be added
     */
    public void addFrontend(final Frontend<K, V> frontend) {
        if (frontend == null) throw new NullPointerException();
        log.info("Adding frontend " + frontend);
        this.frontends.add(frontend);
        assert this.frontends.contains(frontend);
    }

    /**
     * Remove a frontend.
     *
     * @param frontend to be removed
     */
    public void removeFrontend(final Frontend<K, V> frontend) {
        if (frontend == null) throw new NullPointerException();
        log.info("Removing frontend " + frontend);
        this.frontends.remove(frontend);
        assert !this.frontends.contains(frontend);
    }

    /**
     * Update all frontends to use the current primary.
     */
    private void notifyFrontends() {
        log.info("Notifying frontends of what the primary is...");
        for (Frontend<K, V> frontend: this.frontends) {
            frontend.setPrimary(this.primaryStub);
        }
    }

    /**
     * Randomly assign a primary from amongst all currently registered replicas.
     *
     * @throws RemoteException
     */
    private void assignNewPrimary() throws RemoteException {
        assert this.primaryStub == null;
        log.info("Attempting to assign a new primary...");
        final Iterator<PassiveReplica<K, V>> iterator = this.currentSnapshot.iterator();
        if (iterator.hasNext()) {
            this.primaryStub = iterator.next();
            log.info("New primary: " + this.primaryStub);
            this.updatePrimary();
            this.notifyFrontends();
        } else {
            log.warning("No replicas available to become primary.");
        }
    }

    /**
     * Sets the primary of the system to null.
     */
    private void unsetPrimary() {
        log.entering(this.getClass().getName(), "unsetPrimary");
        // firstly tell all frontends there is no longer a primary
        PassiveReplica<K, V> oldPrimary = this.primaryStub;
        this.primaryStub = null;
        this.notifyFrontends();

        // if there was a primary before, clear its secondaries
        if (oldPrimary != null) {
            log.info("Unsetting the current primary...");
            log.info("Current primary being notified to clear its secondaries.");
            try {
                oldPrimary.clearSecondaries();
            } catch (final RemoteException e) {
                log.log(Level.SEVERE, "Could not get a hold of primary to notify it to clear its stores!", e);
                // even if the old primary's secondaries can't be cleared, this should be OK
                // as it's not going to receive any more requests from frontends
            }
        }
        // reset snapshots!
        this.currentSnapshot = new HashSet<>(3);
        this.previousSnapshot = new HashSet<>(3);
        log.exiting(this.getClass().getName(), "unsetPrimary");
    }

    /**
     * Notify the primary of all the servers in the directory, except itself.
     * @throws RemoteException
     */
    private void updatePrimary() throws RemoteException {
        if (this.primaryStub == null) {
            return;
        }

        // add newly added secondaries
        for (final PassiveReplica<K, V> current : this.currentSnapshot) {
            if (
                    !this.previousSnapshot.contains(current) &&
                    (current != this.primaryStub)
            ) {
                log.info("Notifying primary of new secondary replica: " + current);
                this.primaryStub.addSecondary(current);
            }
        }

        // remove non-reachable secondaries
        for (final PassiveReplica<K, V> old : this.previousSnapshot) {
            if (!this.currentSnapshot.contains(old)) {
                log.info("Notifying primary of unregistered replica: " + old);
                this.primaryStub.removeSecondary(old);
            }
        }
    }

    private boolean isPrimaryStillRegistered() throws RemoteException {
        if (this.primaryStub == null) throw new IllegalStateException();
        return this.currentSnapshot.contains(this.primaryStub);
    }

    @Override
    public void run() {
        log.info("Manager starting running for directory " + this.directory);
        while (!Thread.interrupted()) {
            log.info("Polling directory for changes...");
            try {
                this.currentSnapshot = this.directory.getRegistered();
            } catch (RemoteException e) {
                log.log(Level.SEVERE, "Directory is unreachable", e);
                break;
            }

            try {
                // update snapshot
                // if no primary, try to set one
                if (this.primaryStub == null) {
                    this.assignNewPrimary();
                }
                // if there is a primary
                if (this.primaryStub != null) {
                    if (this.isPrimaryStillRegistered()) {
                        // notify primary of any updates to directory if
                        // primary is still available
                        this.updatePrimary();
                    } else {
                        // if primary is not available, unset it
                        this.unsetPrimary();
                    }
                }
            } catch (final RemoteException e) {
                log.log(Level.SEVERE, "??? is unreachable", e);
            } finally {
                this.previousSnapshot = this.currentSnapshot;
            }

            try {
                Thread.sleep(this.timeBetweenPollInMillis);
            } catch (final InterruptedException e) {
                log.log(Level.WARNING, "Was interrupted", e);
            }
        }
    }
}
