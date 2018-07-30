package wfjv99.moviesys.frontend;

import wfjv99.util.Pingable;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Periodically pings remotes in a directory and removes them from the directory
 * once they are unreachable.
 */
public class Monitor<R extends Pingable> implements Runnable {

    private final static Logger log = Logger.getLogger(Monitor.class.getName());

    private final Directory<R> directory;
    private final long timeBetweenPingsInMillis;

    public Monitor(Directory<R> directory,  long timeBetweenPingsInMillis) {
        if (directory == null) throw new NullPointerException();
        if (timeBetweenPingsInMillis <= 0) throw new IllegalArgumentException();
        log.info("Instantiating monitor...");
        this.directory = directory;
        this.timeBetweenPingsInMillis = timeBetweenPingsInMillis;
    }

    @Override
    public void run() {
        log.info("Monitor now running for directory " + this.directory);
        while (!Thread.interrupted()) {
            try {
                // periodically ping remotes
                // if a remote is unreachable, remove it from directory
                for (R r : this.directory.getRegistered()) {
                    try {
                        r.ping();
                    } catch (RemoteException e) {
                        log.log(Level.INFO, r + " has become unreachable.", e);
                        this.directory.unregister(r);
                    }
                }
            } catch (RemoteException e) {
                log.log(Level.SEVERE, "Directory is unreachable", e);
            }

            try {
                log.fine("Sleeping " + this.timeBetweenPingsInMillis + " milliseconds...");
                Thread.sleep(this.timeBetweenPingsInMillis);
            } catch (InterruptedException e) {
                log.log(Level.WARNING, "Was interrupted while sleeping", e);
            }
        }
    }
}
