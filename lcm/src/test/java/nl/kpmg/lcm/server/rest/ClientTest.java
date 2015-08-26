package nl.kpmg.lcm.server.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import nl.kpmg.lcm.server.authentication.AuthenticationManager;
import nl.kpmg.lcm.server.LCMBaseServerTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.rest.types.LoginRequest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ClientTest extends LCMBaseServerTest {

    @Autowired
    private AuthenticationManager am;

    /**
     * Test to see that the client interface returns the interface versions.
     *
     * @throws ServerException
     */
    @Test
    public void testGetClientInterfaceVersions() throws LoginException {
        String expected = "[\"v0\"]";

        // Due to a Spring configuration error we can't login in this thread. We
        // have to create a actuall login call.
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin");
        Entity<LoginRequest> entity = Entity.entity(loginRequest,
                "application/nl.kpmg.lcm.server.rest.client.version0.types.LoginRequest+json");
        Response res = target
                .path("client/login")
                .request()
                .post(entity);

        Response result = target
                .path("client")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", res.readEntity(String.class))
                .get();
        assertEquals(200, result.getStatus());
        assertEquals(expected, result.readEntity(String.class));
    }
}
