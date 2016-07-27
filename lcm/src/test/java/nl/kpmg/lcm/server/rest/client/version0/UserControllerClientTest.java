package nl.kpmg.lcm.server.rest.client.version0;

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
import nl.kpmg.lcm.server.rest.authentication.UserPasswordHashException;
import nl.kpmg.lcm.server.data.dao.UserDao;
import nl.kpmg.lcm.server.rest.client.version0.types.UserRepresentation;
import nl.kpmg.lcm.server.rest.client.types.LoginRequest;
import org.junit.After;
import static org.junit.Assert.assertNull;
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

        UserDao userDao = userService.getUserDao();
        for (User user : userDao.findAll()) {
            userDao.delete(user);
        }
    }

    @Test
    public void testGetUserTarget() throws LoginException, UserPasswordHashException {
        UserDao userDao = userService.getUserDao();
        User expected = new User();
        expected.setName("testUser");
        expected.setPassword("testPassword");

        User saved = userDao.save(expected);

        Response response = target
                .path("client/v0/users/" + saved.getId())
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .get();

        assertEquals(200, response.getStatus());

        User actual = response.readEntity(UserRepresentation.class).getItem();
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getRole(), actual.getRole());

        // We have been facing a race condition with SecurityEntityFilteringFeature
        // If this test fails sporadically search there.
        assertNull("The password should NEVER be returned via the interface", actual.getPassword());
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
        String readEntity = res1.readEntity(String.class);
        System.out.println(readEntity);
    }

    @Test
    @Ignore("The save user target suffers from issues. The serialisation"
            + "of passwords should be handled differently. Currently this "
            + "functionality is broken.")
    public void testSaveUserWebTarget() throws LoginException, UserPasswordHashException {
        User user = new User();
        user.setName("admin");
        user.setPassword("admin");
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
    @Ignore("The save user target suffers from issues. The serialisation"
            + "of passwords should be handled differently. Currently this "
            + "functionality is broken.")
    public void testModifyUserWebTarget() throws LoginException, UserPasswordHashException {
        UserDao userDao = userService.getUserDao();

        User user = new User();
        user.setName("admin123");
        user.setPassword("admin");
        User saved = userDao.save(user);

        saved.setPassword("admin123");

        Response res1 = target
                .path("client/v0/users/" + saved.getId())
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .put(Entity.entity(saved, "application/nl.kpmg.lcm.server.data.User+json"));

        assertEquals(200, res1.getStatus());

        User actual = userDao.findOneByName("admin123");
        assertTrue(actual.passwordEquals("admin123"));
    }

    @Test
    public void testDeleteUserWebTarget() throws LoginException, UserPasswordHashException {
        UserDao userDao = userService.getUserDao();
        assertNull(userDao.findOneByName("admin123"));

        User user = new User();
        user.setName("admin123");
        user.setPassword("admin");
        User saved = userDao.save(user);

        Response response;

        // Delete the user
        response = target
                .path("client/v0/users/" + saved.getId())
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .delete();
        assertEquals(200, response.getStatus());

        assertNull(userDao.findOne(saved.getId()));
        assertNull(userDao.findOneByName(saved.getName()));
    }

    @Test
    public void testLogoutUser() throws LoginException, UserPasswordHashException {
        User user = new User();
        user.setName("admin");
        user.setPassword("admin");
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
