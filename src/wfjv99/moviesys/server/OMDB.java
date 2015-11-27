package wfjv99.moviesys.server;

import wfjv99.util.RemoteSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OMDB implements RemoteSource<String, String> {

    public static final String BASE_URL = "http://www.omdbapi.com";
    private static final Logger log = Logger.getLogger(OMDB.class.getName());

    static {
        // essentially return XML as a string
        URLConnection.setContentHandlerFactory(new ContentHandlerFactory() {
            @Override
            public ContentHandler createContentHandler(String mimetype) {
                return new ContentHandler() {
                    @Override
                    public Object getContent(final URLConnection urlc) throws IOException {
                        try (InputStream in = urlc.getInputStream()) {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                                return reader.readLine();
                            }
                        }
                    }
                };
            }
        });
    }

    @Override
    public String get(String imdbid) throws RemoteException {
        log.info("Received request to fetch info about " + imdbid);
        try {
            final String query = "?i=" + imdbid + "&plot=short&r=xml";
            final URL finalUrl = new URL(new URL(OMDB.BASE_URL), query);
            final URLConnection connection = finalUrl.openConnection();
            return (String) connection.getContent();
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, "Malformed URL: ", e);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error fetching movie info", e);
        }
        return null;
    }
}
