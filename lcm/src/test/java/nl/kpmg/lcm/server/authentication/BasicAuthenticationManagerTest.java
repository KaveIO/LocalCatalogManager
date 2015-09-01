package nl.kpmg.lcm.server.authentication;

import javax.ws.rs.core.Response;
import nl.kpmg.lcm.server.LCMBaseServerTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.authentication.BasicAuthenticationManager;
import org.apache.commons.codec.binary.Base64;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author venkateswarlub
 *
 */
public class BasicAuthenticationManagerTest extends LCMBaseServerTest {

    @Autowired
    private BasicAuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    /**
     * Token used for login as admin: Basic BASE64(admin + ":" + admin)
     */
    private String basicAuthTokenAdmin = "Basic YWRtaW46YWRtaW4=";

    @Test
    public void testGetUserTarget() throws LoginException {
        Response res1 = target
                .path("client/v0/users")
                .request()
                .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin)
                .get();

        assertEquals(200, res1.getStatus());
    }

    @Test
    public void testGetLocalTarget() throws LoginException {
        Response res1 = target
                .path("client/v0/local")
                .request()
                .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin)
                .get();

        assertEquals(200, res1.getStatus());
    }
}
