package nl.kpmg.lcm.server.rest.client.version0;

import static org.junit.Assert.assertEquals;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.Test;
import nl.kpmg.lcm.server.LCMBaseServerTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.rest.authentication.BasicAuthenticationManager;

public class LocalControllerClientTest extends LCMBaseServerTest {

    @Test
    public void testGetLocalOverview() throws LoginException {
        Response response = target
                .path("client/v0/local")
                .request()
                .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin)
                .get();

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
    }
}
