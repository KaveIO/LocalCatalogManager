package nl.kpmg.lcm.server;


import nl.kpmg.lcm.ui.Client;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.WebTarget;

public abstract class LCMBaseServerTest extends LCMBaseTest {

  /**
   * Token used for login as admin: Basic BASE64(admin + ":" + admin)
   */
  protected static String basicAuthTokenAdmin = "Basic YWRtaW46YWRtaW4=";

  protected static Server server;
  private static Client client;
  protected static WebTarget target;

  @BeforeClass
  public static void setUpClass() throws Exception {
    try {
      LCMBaseTest.setUpClass();
      server = new Server();
      server.start();
      client = new Client();
      target = client.createWebTarget(server.getBaseUri());
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
}
