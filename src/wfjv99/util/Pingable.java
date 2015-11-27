package wfjv99.util;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Pingable extends Remote {

    void ping() throws RemoteException;
}
