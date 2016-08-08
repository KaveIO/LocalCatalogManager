package nl.kpmg.lcm.server;

import nl.kpmg.lcm.HttpsClientFactory;
import nl.kpmg.lcm.configuration.ClientConfiguration;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.WebTarget;

public abstract class LCMBaseServerTest extends LCMBaseTest {

  /**
   * Token used for login as admin: Basic BASE64(admin + ":" + admin)
   */
  protected static String basicAuthTokenAdmin = "Basic YWRtaW46YWRtaW4=";

  protected static Server server;

  protected static HttpsClientFactory httpsClientFactory;

  @BeforeClass
  public static void setUpClass() throws Exception {
    try {
      LCMBaseTest.setUpClass();
      server = new Server();
      server.start();

      ClientConfiguration clientConfiguration = new ClientConfiguration();
      clientConfiguration.setKeystore("./src/test/resources/client-keystore.jks");
      clientConfiguration.setKeystoreType("JKS");
      clientConfiguration.setKeystorePassword("storepass");
      clientConfiguration.setKeystoreAlias("cert-ui");
      clientConfiguration.setKeystoreKeypass("keypass");
      clientConfiguration.setTruststore("./src/test/resources/client-truststore.jks");
      clientConfiguration.setTruststorePassword("storepass");
      clientConfiguration.setTruststoreType("JKS");

      httpsClientFactory = new HttpsClientFactory(clientConfiguration);

    } catch (ServerException se) {
      Logger.getLogger(LCMBaseServerTest.class.getName()).log(Level.SEVERE,
          "Failed to create HTTPS server or client, test will fail", se);
    }
  }

  @AfterClass
  public static void tearDownClass() {
    LCMBaseTest.tearDownClass();
    server.stop();
  }

  protected WebTarget getWebTarget() throws ServerException {
    return httpsClientFactory.createWebTarget(server.getBaseUri());
  }
}
