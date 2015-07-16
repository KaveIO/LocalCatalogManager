package nl.kpmg.lcm.server.rest;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import nl.kpmg.lcm.server.Configuration;
import nl.kpmg.lcm.server.Server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ClientTest {

    private Server server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        // start the server
        server = new Server(new Configuration());
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