package nl.kpmg.lcm.server;

import java.io.IOException;
import java.net.URI;
import nl.kpmg.lcm.server.metadata.MetaData;
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
    private HttpServer restServer;

    public Server(String[] arguments) {
        /** @TODO Parse arguments, probably abstract argument parsing */
        baseUri = "http://localhost:8080/";
    }
    
    
    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public HttpServer startRestInterface() {
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
