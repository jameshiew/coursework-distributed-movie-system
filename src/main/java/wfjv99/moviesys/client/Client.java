package wfjv99.moviesys.client;

import wfjv99.util.RemoteSource;
import wfjv99.util.RemoteStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.*;
import java.util.logging.Logger;

/**
 * Use raw System.in, System.out.
 */
public class Client {

    private static final Logger log = Logger.getLogger(Client.class.getName());

    private final Remote frontend;

    public Client(final Remote frontend) {
        log.fine("Instantiating client...");
        if (frontend == null) throw new NullPointerException();
        this.frontend = frontend;
    }

    public void repl() {
        log.fine("Starting REPL loop.");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        do {
            System.out.print("> ");
            try {
                input = in.readLine();
            } catch (IOException e) {
                System.out.println("Could not read from standard input; exiting");
                System.exit(1);
            }
            final String[] tokens = input.split(" ");
            if (tokens.length < 2 || 3 < tokens.length) continue;
            final String command = tokens[0];
            final String id = tokens[1];
            try {
                switch (command) {
                    case "get":
                        final String result = ((RemoteSource<String, String>) this.frontend).get(id);
                        if (result == null) {
                            System.out.println("There is a problem with the movie system, and it could not get information for your id.");
                        } else {
                            System.out.println(result);
                        }
                        break;
                    case "set":
                        if (tokens.length < 3) {
                            System.out.println("Please provide a third argument.");
                            continue;
                        }
                        ((RemoteStore<String, String>) this.frontend).set(id, tokens[2]);
                        break;
                    default:
                        System.out.println("Did not understand command, please use `get [id]` or `set [id] [value]`");
                        break;
                }
            } catch (ServerException e) {
                System.out.println("Message from server: " + e.getMessage());
            } catch (ConnectException|ConnectIOException e) {
                System.out.println("Frontend has become unreachable, exiting...");
                System.exit(1);
            } catch (RemoteException e) {
                System.out.println("Unhandled remote exception.");
                e.printStackTrace();
            }
        } while (!("exit".equals(input)));
    }
}
