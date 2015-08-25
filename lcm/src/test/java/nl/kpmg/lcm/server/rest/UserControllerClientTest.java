package nl.kpmg.lcm.server.rest;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import static org.junit.Assert.assertEquals;


import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import nl.kpmg.lcm.server.authentication.AuthenticationManager;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.service.EncryptDecryptService;
import nl.kpmg.lcm.server.data.service.UserService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import nl.kpmg.lcm.server.LCMBaseServerTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.rest.client.version0.types.LoginRequest;
import org.junit.After;
import org.junit.Before;

public class UserControllerClientTest extends LCMBaseServerTest {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager am;
    @Autowired
    private EncryptDecryptService encdecService;

    private String authenticationToken;

    @Before
    public void beforeTest() {
        // Due to a Spring configuration error we can't login in this thread. We
        // have to create a actuall login call.
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin");
        Entity<LoginRequest> entity = Entity.entity(loginRequest,
                "application/nl.kpmg.lcm.server.rest.client.version0.types.LoginRequest+json");
        Response res = target
                .path("client/v0/users/login")
                .request()
                .post(entity);
        authenticationToken = res.readEntity(String.class);
    }

    @After
    public void afterTest() {
        target
                .path("client/v0/users/logout")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .post(null);
    }

    @Test
    public void testGetUserTarget() throws LoginException {
        Response res1 = target
                .path("client/v0/users/test")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .get();

        assertEquals(200, res1.getStatus());
    }

    @Test
    public void testGetUsersWebTarget() throws LoginException {
        Response res1 = target
                .path("client/v0/users")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .get();

        assertEquals(200, res1.getStatus());
    }

    @Test
    public void testGetUserWebTarget() throws LoginException {
        Response res1 = target
                .path("client/v0/users/admin")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .get();

        assertEquals(200, res1.getStatus());
    }

    @Test
    public void testSaveUserWebTarget() throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("admin", false);
        Entity<User> entity = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");
        Response res1 = target
                .path("client/v0/users/admin")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .put(entity);

        assertEquals(200, res1.getStatus());
    }

    @Test
    public void testModifyUserWebTarget() throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException {
        User user = new User();

        user.setUsername("admin123");
        user.setPassword("admin", false);
        Entity<User> entity1 = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");

        Response res1 = target
                .path("client/v0/users/admin")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .put(entity1);

        assertEquals(200, res1.getStatus());
    }

    @Test
    public void testDeleteUserWebTarget() throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("admin", false);
        Entity<User> entity = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");

        Response res1 = target
                .path("client/v0/users/admin")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .delete();

        assertEquals(200, res1.getStatus());
    }

    @Test
    public void testLogoutUser() throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("admin", false);
        Entity<User> entity = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");

        Response res1 = target
                .path("client/v0/users/logout")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .post(entity);

        assertEquals(200, res1.getStatus());
    }
}
