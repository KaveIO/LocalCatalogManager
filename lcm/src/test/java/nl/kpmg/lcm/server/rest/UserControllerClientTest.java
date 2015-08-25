package nl.kpmg.lcm.server.rest;

import nl.kpmg.lcm.server.LCMBaseTest;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import nl.kpmg.lcm.server.AuthenticationManager;
import nl.kpmg.lcm.server.JacksonJsonProvider;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.service.EncryptDecryptService;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.client.version0.UserController;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.rest.client.version0.types.LoginRequest;
import org.junit.After;
import org.junit.Before;

public class UserControllerClientTest extends LCMBaseTest {

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
    public void testSaveUserWebTarget() throws LoginException {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("admin");
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
    public void testModifyUserWebTarget() throws LoginException {
        User user = new User();

        user.setUsername("admin123");
        user.setPassword("admin");
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
    public void testDeleteUserWebTarget() throws LoginException {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("admin");
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
    public void testLogoutUser() throws LoginException {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("admin");
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
