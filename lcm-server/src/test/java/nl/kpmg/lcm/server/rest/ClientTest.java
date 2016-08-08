package nl.kpmg.lcm.server.rest;

import static org.junit.Assert.assertEquals;

import nl.kpmg.lcm.server.LCMBaseServerTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.rest.authentication.SessionAuthenticationManager;
import nl.kpmg.lcm.server.rest.client.types.LoginRequest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class ClientTest extends LCMBaseServerTest {

  /**
   * Test to see that the client interface returns the interface versions.
   *
   * @throws ServerException
   */
  @Test
  public void testGetClientInterfaceVersions() throws LoginException, ServerException {
    // Due to a Spring configuration error we can't login in this thread. We
    // have to create a actuall login call.
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setUsername("admin");
    loginRequest.setPassword("admin");
    Entity<LoginRequest> entity = Entity.entity(loginRequest,
        "application/nl.kpmg.lcm.server.rest.client.types.LoginRequest+json");
    Response res = getWebTarget().path("client/login").request().post(entity);

    Response result =
        getWebTarget().path("client").request().header("LCM-Authentication-User", "admin")
            .header("LCM-Authentication-Token", res.readEntity(String.class)).get();
    assertEquals(200, result.getStatus());
  }
}
