package wfjv99.moviesys.client;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {

    /**
     * USAGE: java wfjv99.moviesys.client.Main [host] [port]
     */
    public static void main(final String[] args) {
        final String host = args[0];
        final int port = Integer.parseInt(args[1]);

        try {
            final Registry registry = LocateRegistry.getRegistry(host, port);
            final Remote frontend = registry.lookup("frontend");
            new Client(frontend).repl();
        } catch (RemoteException e) {
            System.out.println("Could not locate registry at " + host + ":" + port);
        } catch (NotBoundException e) {
            System.out.println("No 'frontend' registered with registry.");
        }
    }
}
