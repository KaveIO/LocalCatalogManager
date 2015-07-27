package nl.kpmg.lcm.server.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ClientTest extends LCMBaseTest {
    
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