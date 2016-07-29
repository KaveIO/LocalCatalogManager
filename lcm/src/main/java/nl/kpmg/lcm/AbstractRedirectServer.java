package nl.kpmg.lcm;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import nl.kpmg.lcm.server.ServerException;

/**
 *
 * @author mhoekstra
 */
public abstract class AbstractRedirectServer {

    private static final Logger LOGGER = Logger.getLogger(AbstractRedirectServer.class.getName());
        
    protected BasicConfiguration configuration;

    protected ApplicationContext parentContext = new ClassPathXmlApplicationContext(new String[] { "application-context.xml" });
    
    private HttpServer restServer;

    /**
     * Starts Grizzly HTTP server that redirects the connection from HTTP to HTTPS
     *
     * @return Grizzly HTTP server.
     */
    public HttpServer startRedirectServer() {
        HttpServer server;

        LOGGER.info("Starting redirect server...");
        
        server = HttpServer.createSimpleServer(null, configuration.getServicePort());
        server.getServerConfiguration().addHttpHandler(
                new HttpHandler() {
            @Override
            public void service(Request request, Response response) throws Exception {
                response.setStatus(HttpStatus.MOVED_PERMANENTLY_301);
                //Redirecting to the secure server
                response.setHeader(Header.Location, "https://" + configuration.getServiceName() + ":" + configuration.getSecureServicePort() + request.getRequestURI());
            }
        }, "");

        return server;
    }

    /**
     * Main method.
     *
     * @throws ServerException
     */
    public void start() throws ServerException {
    		if (!configuration.isUnsafe())
	        try {
	            restServer = startRedirectServer();
	            restServer.start();
	        }
	        catch (IOException ex) {
	            Logger.getLogger(AbstractRedirectServer.class.getName()).log(Level.SEVERE, null, ex);
	        }
    }

    public void stop() {
    		if (restServer != null)
    			restServer.shutdownNow();
    }

}
