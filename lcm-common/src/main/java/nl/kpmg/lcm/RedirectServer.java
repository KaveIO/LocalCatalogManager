package nl.kpmg.lcm;

import nl.kpmg.lcm.server.ServerException;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedirectServer {

  private static final Logger LOGGER = Logger.getLogger(RedirectServer.class.getName());

  private final int servicePort;

  private final int serviceSecurePort;

  private final int serviceName;

  private HttpServer restServer;

  public RedirectServer(int servicePort, int serviceSecurePort, int serviceName) {
    this.servicePort = servicePort;
    this.serviceSecurePort = serviceSecurePort;
    this.serviceName = serviceName;
  }

  /**
   * Main method.
   *
   * @throws ServerException
   */
  public void start() throws ServerException {
    try {
      if (restServer == null) {
        restServer = startRedirectServer();
        restServer.start();
      } else {
        LOGGER.log(Level.SEVERE, "Redirect server already running");
      }
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, "Failed to start Redirect Server", ex);
    }
  }

  public void stop() {
    if (restServer != null) {
      restServer.shutdownNow();
    }
  }

  /**
   * Starts Grizzly HTTP server that redirects the connection from HTTP to HTTPS
   *
   * @return Grizzly HTTP server.
   */
  private HttpServer startRedirectServer() {
    HttpServer server;

    LOGGER.info("Starting redirect server...");

    server = HttpServer.createSimpleServer(null, servicePort);
    server.getServerConfiguration().addHttpHandler(new HttpHandler() {
      @Override
      public void service(Request request, Response response) throws Exception {
        response.setStatus(HttpStatus.MOVED_PERMANENTLY_301);
        // Redirecting to the secure server
        response.setHeader(Header.Location,
            "https://" + serviceName + ":" + serviceSecurePort + request.getRequestURI());
      }
    }, "");

    return server;
  }
}
