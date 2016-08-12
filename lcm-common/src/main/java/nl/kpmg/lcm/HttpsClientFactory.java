package nl.kpmg.lcm;

import nl.kpmg.lcm.configuration.ClientConfiguration;
import nl.kpmg.lcm.server.ServerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

@Component
public class HttpsClientFactory {
  private static final Logger LOGGER = Logger.getLogger(HttpsClientFactory.class.getName());

  private final ClientConfiguration configuration;

  @Autowired
  public HttpsClientFactory(ClientConfiguration configuration) {
    this.configuration = configuration;
  }

  private Client create() {
    SSLContext sc;
    ClientBuilder builder = ClientBuilder.newBuilder();
    try {
      sc = SslProvider.createSSLContextConfigurator(configuration).createSSLContext();
    } catch (SslConfigurationException ex) {
      LOGGER.log(Level.WARNING,
          "Invalid SSL configuration, client will contact the configured server on HTTP only");
      return builder.build();
    }
    try {
      builder = builder.sslContext(sc);
    } catch (NullPointerException npe) {
      LOGGER.log(Level.WARNING, "Null SSL context, skipping client SSL configuration", npe);
    }
    return builder.build();
  }

  public WebTarget createWebTarget(String targetURI) throws ServerException {
    try {
      Client client = create();
      WebTarget webTarget = client.target(targetURI);
      return webTarget;
    } catch (NullPointerException npe) {
      LOGGER.log(Level.SEVERE, "Cannot create the web target, null target uri", npe);
      throw new ServerException(npe);
    } catch (IllegalArgumentException iae) {
      LOGGER.log(Level.SEVERE, "Cannot create the web target, malformed target uri", iae);
      throw new ServerException(iae);
    }
  }
}

