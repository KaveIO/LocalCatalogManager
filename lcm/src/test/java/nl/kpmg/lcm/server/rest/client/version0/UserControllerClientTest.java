package nl.kpmg.lcm.server.rest.client.version0;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import static org.junit.Assert.assertEquals;


import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import nl.kpmg.lcm.server.rest.authentication.SessionAuthenticationManager;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.service.UserService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import nl.kpmg.lcm.server.LCMBaseServerTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.data.dao.UserDao;
import nl.kpmg.lcm.server.rest.client.version0.types.UserRepresentation;
import nl.kpmg.lcm.server.rest.client.types.LoginRequest;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;

public class UserControllerClientTest extends LCMBaseServerTest {

    @Autowired
    private UserService userService;

    @Autowired
    private SessionAuthenticationManager am;

    private String authenticationToken;

    @Before
    public void beforeTest() {
        // Due to a Spring configuration error we can't login in this thread. We
        // have to create a actuall login call.
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin");
        Entity<LoginRequest> entity = Entity.entity(loginRequest,
                "application/nl.kpmg.lcm.server.rest.client.types.LoginRequest+json");
        Response res = target
                .path("client/login")
                .request()
                .post(entity);
        authenticationToken = res.readEntity(String.class);
    }

    @After
    public void afterTest() {
        target
                .path("client/logout")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .post(null);
    }

    @Test
    @Ignore
    public void testGetUserTarget() throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException {
        UserDao userDao = userService.getUserDao();
        User expected = new User();
        expected.setId("testUser");
        expected.setPassword("testPassword");
        userDao.persist(expected);

        Response response = target
                .path("client/v0/users/testUser")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .get();

        assertEquals(200, response.getStatus());


        User actual = response.readEntity(UserRepresentation.class).getItem();
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getRole(), actual.getRole());
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
    public void testSaveUserWebTarget() throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException {
        User user = new User();
        user.setId("admin");
        user.setPassword("admin", false);
        Entity<User> entity = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");
        Response res1 = target
                .path("client/v0/users")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .post(entity);

        assertEquals(200, res1.getStatus());
    }

    @Test
    public void testModifyUserWebTarget() throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException {
        UserDao userDao = userService.getUserDao();

        User user = new User();
        user.setId("admin123");
        user.setPassword("admin");
        userDao.persist(user);


        user.setId("admin123");
        user.setPassword("admin123", false);

        Response res1 = target
                .path("client/v0/users/admin123")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .put(Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json"));

        assertEquals(200, res1.getStatus());

        User actuall = userDao.getById("admin123");
        assertTrue(actuall.passwordEquals("admin123"));
    }

    @Test
    public void testDeleteUserWebTarget() throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException {
        User user = new User();
        user.setId("testUser");
        user.setPassword("testPassword", false);
        Entity<User> entity = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");

        Response response;

        // Lets create the user
        response = target
                .path("client/v0/users")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .post(entity);
        assertEquals(200, response.getStatus());

        // Can we retrieve the user?
        response = target
                .path("client/v0/users/" + user.getId())
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .get();
        assertEquals(200, response.getStatus());

        // Now delete the user
        response = target
                .path("client/v0/users/" + user.getId())
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .delete();
        assertEquals(200, response.getStatus());

        // Now the user better is gone
        response = target
                .path("client/v0/users/" + user.getId())
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .get();
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testLogoutUser() throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException {
        User user = new User();
        user.setId("admin");
        user.setPassword("admin", false);
        Entity<User> entity = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");

        Response res1 = target
                .path("client/logout")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .post(entity);

        assertEquals(200, res1.getStatus());
    }
}
