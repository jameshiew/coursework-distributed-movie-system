package wfjv99.util;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteSource<K extends Serializable, V extends Serializable> extends Remote {

    V get(K k) throws RemoteException;
}
