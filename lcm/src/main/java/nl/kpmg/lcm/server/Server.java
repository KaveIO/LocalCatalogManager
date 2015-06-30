package nl.kpmg.lcm.server;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.metadata.MetaData;
import nl.kpmg.lcm.server.metadata.storage.StorageException;
import nl.kpmg.lcm.server.metadata.storage.file.MetaDataDaoImpl;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

/**
 *
 * @author mhoekstra
 */
public class Server {
    private final String baseUri;
    private final String storagePath;

    private HttpServer restServer;

    public Server(String[] arguments) {
        /** @TODO Parse arguments, probably abstract argument parsing */
        baseUri = "http://localhost:8080/";
        storagePath = "./metadata/";

        try {
            Resources.setMetaDataDao(new MetaDataDaoImpl(storagePath));
        } catch (StorageException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE,
                    "Construction of MetaDataDaoImpl failed. The storage path doesn't exist", ex);
        }
    }


    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public HttpServer startRestInterface() {
        try {
            MetaDataDaoImpl metaDataDaoImpl = new MetaDataDaoImpl("./metadata");
            // create a resource config that scans for JAX-RS resources and providers
            // in com.example package
            final ResourceConfig rc = new ResourceConfig()
                    .packages("nl.kpmg.lcm.server.rest")
                    .registerClasses(JacksonFeature.class)
                    .registerClasses(JacksonJsonProvider.class)
                    .registerClasses(MetaData.class);

            // create and start a new instance of grizzly http server
            // exposing the Jersey application at BASE_URI
            return GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), rc);
        }
        catch (StorageException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Main method.
     * @throws IOException
     */
    public void start() throws IOException {
        restServer = startRestInterface();
    }

    public void stop() {
        restServer.stop();
    }

    public String getBaseUri() {
        return baseUri;
    }
}
