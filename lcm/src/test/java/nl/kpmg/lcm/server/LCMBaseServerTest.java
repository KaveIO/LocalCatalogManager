package nl.kpmg.lcm.server;


import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class LCMBaseServerTest extends LCMBaseTest {

    /**
     * Token used for login as admin: Basic BASE64(admin + ":" + admin)
     */
    protected static String basicAuthTokenAdmin = "Basic YWRtaW46YWRtaW4=";

    protected static Server server;
    protected static WebTarget target;

    @BeforeClass
    public static void setUpClass() throws Exception {
        LCMBaseTest.setUpClass();
        try {
            server = new Server();
            server.start();
        }
        catch (ServerException e) {
            e.printStackTrace();
        }

        // create the client
        target = ClientBuilder.newClient().target(server.getBaseUri());
    }

    @AfterClass
    public static void tearDownClass() {
        LCMBaseTest.tearDownClass();
        server.stop();
    }
}
