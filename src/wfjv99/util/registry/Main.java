package wfjv99.util.registry;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Simple registry that knows about all the classes.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        java.rmi.registry.Registry registry = LocateRegistry.createRegistry(port);
        System.out.println("Registry running.");
        // list out registered things every keypress
        // stop on EOF (Ctrl+D)
        int in = 0;
        do {
            in = System.in.read();
            String[] list = registry.list();
            System.out.println(Integer.toString(list.length) + " registered remotes.");
            for (String s : list) {
                System.out.println(s);
            }
        } while (in != -1);
        System.out.println("Stopping registry...");
    }
}
