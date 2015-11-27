package wfjv99.moviesys.frontend;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface Directory<R extends Remote> extends Remote {

    void register(R r) throws RemoteException;
    void unregister(R r) throws RemoteException;
    Set<R> getRegistered() throws RemoteException;
}
