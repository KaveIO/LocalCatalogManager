package nl.kpmg.lcm.server.rest;

import java.io.File;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import nl.kpmg.lcm.server.Server;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;

public class ClientTest {
    private static final String TEST_STORAGE_PATH = "test";

    private Server server;
    private WebTarget target;

    @BeforeClass
    public static void setUpClass() {
        File file;
        file = new File(TEST_STORAGE_PATH);
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/metadata");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/taskdescription");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/taskschedule");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/backend");
        file.mkdir();
    }

    @AfterClass
    public static void tearDownClass() {
        File file;
        file = new File(TEST_STORAGE_PATH);
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/metadata");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/taskdescription");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/taskschedule");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/backend");
        file.delete();
    }

    @Before
    public void setUp() throws Exception {
        // start the server
        server = new Server();
        server.start();

        // create the client
        target = ClientBuilder.newClient().target(server.getBaseUri());
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    /**
     * Test to see that the client interface returns the interface versions.
     */
    @Test
    public void testGetClientInterfaceVersions() {
        String expected = "[\"v0\"]";
        String actual = target
                .path("client")
                .request()
                .get(String.class);
        assertEquals(expected, actual);
    }
}