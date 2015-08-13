package nl.kpmg.lcm.server.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import nl.kpmg.lcm.server.AuthenticationManager;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.User;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ClientTest extends LCMBaseTest {
    
	@Autowired
	private AuthenticationManager am;
	/**
     * Test to see that the client interface returns the interface versions.
	 * @throws ServerException 
     */
    @Test
    public void testGetClientInterfaceVersions() throws ServerException {
        String expected = "[\"v0\"]";
        User user = new User();
        user.setUsername("admin");
        user.setPassword("admin");
        Entity<User> entity = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");
        am.getAuthentication(user.getUsername(), user.getPassword(), "ABC123");
        Response res = target
                .path("client/v0/users/login").queryParam("serviceKey", "ABC123")
                .request().post(entity);
        String actual = target
                .path("client").queryParam("serviceKey", "ABC123").queryParam("authorizationToken", "AUTH_TOKEN")
                .request()
                .get(String.class);
        assertEquals(200, res.getStatus());
        assertEquals(expected, actual);
    }
}