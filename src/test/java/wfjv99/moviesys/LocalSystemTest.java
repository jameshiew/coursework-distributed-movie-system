package wfjv99.moviesys;

import wfjv99.moviesys.frontend.Directory;
import wfjv99.moviesys.frontend.ConcurrentDirectory;
import wfjv99.moviesys.frontend.Frontend;
import wfjv99.moviesys.frontend.Manager;
import wfjv99.moviesys.frontend.Monitor;
import wfjv99.moviesys.PassiveReplica;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LocalSystemTest {

    public static void main(String[] args) throws RemoteException {

        Registry registry = LocateRegistry.createRegistry(55555);

        Frontend<String, String> frontend = new Frontend<>();
        Directory<PassiveReplica<String, String>> directory = new ConcurrentDirectory<>();
        Manager<String, String> manager = new Manager<>(directory, 10000);
        manager.addFrontend(frontend);
        Monitor<PassiveReplica<String, String>> monitor = new Monitor<>(directory, 10000);
    }
}
