package wfjv99.moviesys.frontend;

import wfjv99.moviesys.PassiveReplica;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Main {

    public static void main(String[] args) throws RemoteException {
        final String host = args[0];
        final int port = Integer.parseInt(args[1]);

        final Registry registry = LocateRegistry.getRegistry(host, port);

        final Directory<PassiveReplica<String, String>> directory = new ConcurrentDirectory<>();
        final Remote directoryStub = UnicastRemoteObject.exportObject(directory, 0);
        registry.rebind("directory", directoryStub);

        // monitor pinging every server every 2 seconds
        final Monitor<PassiveReplica<String, String>> monitor = new Monitor<>(directory, 2000);
        final Thread monitorThread = new Thread(monitor);
        monitorThread.start();

        final Frontend<String, String> frontend = new Frontend<>();

        // manager updating the primary server every 2 seconds
        final Manager<String, String> manager = new Manager<>(directory, 2000);
        manager.addFrontend(frontend);
        final Thread prsThread = new Thread(manager);
        prsThread.start();

        // register frontend last once everything is ready
        final Remote frontendStub = UnicastRemoteObject.exportObject(frontend, 0);
        registry.rebind("frontend", frontendStub);

    }
}
