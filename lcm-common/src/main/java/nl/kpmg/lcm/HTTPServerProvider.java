package nl.kpmg.lcm;

import nl.kpmg.lcm.configuration.BasicConfiguration;
import nl.kpmg.lcm.server.ServerException;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPServerProvider {

  private static final Logger LOGGER = Logger.getLogger(HTTPServerProvider.class.getName());

  public static HttpServer createHTTPServer(BasicConfiguration configuration, String baseUri,
      String baseFallbackUri, ResourceConfig rc, boolean clientAuth) throws ServerException {

    // Grizzly ssl configuration
    SSLContextConfigurator sslContextConfigurator;
    try {
      sslContextConfigurator = SSLProvider.createSSLContextConfigurator(configuration);
    } catch (ServerException se) {
      if (configuration.isUnsafe()) {
        LOGGER.log(Level.WARNING,
            "Invalid SSL configuration, server will start on the non-secure port and run on HTTP only");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(baseFallbackUri), rc);
      } else {
        LOGGER.log(Level.SEVERE, "Invalid SSL configuration and unsafe mode off, abort");
        throw se;
      }
    }
    LOGGER.log(Level.INFO, "Configuring SSL context...");
    return GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), rc, true,
        new SSLEngineConfigurator(sslContextConfigurator).setClientMode(false)
            .setNeedClientAuth(clientAuth));
  }
}
