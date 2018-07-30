package wfjv99.moviesys.server;

import wfjv99.moviesys.PassiveReplica;
import wfjv99.moviesys.frontend.Directory;
import wfjv99.util.Propagator;
import wfjv99.util.RemoteSource;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Main {

    public static void main(String[] args) throws RemoteException, NotBoundException {
        final String host = args[0];
        final int port = Integer.parseInt(args[1]);

        // one remote source, the OMDB
        final RemoteSource<String, String> omdb = new OMDB();
        // set up the server
        final Propagator<String, String> server = new Server<>(omdb);
        // no remote stores (to begin with)

        // register server with directory
        final Registry registry = LocateRegistry.getRegistry(host, port);
        final Directory<PassiveReplica<String, String>> directory = (Directory<PassiveReplica<String, String>>) registry.lookup("directory");
        PassiveReplica<String, String> serverStub = (PassiveReplica<String, String>) UnicastRemoteObject.exportObject(server, 0);
        directory.register(serverStub);
    }
}
