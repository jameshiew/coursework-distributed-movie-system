package wfjv99.util;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteStore<K extends Serializable, V extends Serializable> extends Remote {

    void set(K k, V v) throws RemoteException;
}
