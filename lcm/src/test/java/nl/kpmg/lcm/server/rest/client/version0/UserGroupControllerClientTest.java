package nl.kpmg.lcm.server.rest.client.version0;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import nl.kpmg.lcm.server.LCMBaseServerTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.UserGroup;
import nl.kpmg.lcm.server.data.dao.UserGroupDao;
import nl.kpmg.lcm.server.data.service.UserGroupService;
import nl.kpmg.lcm.server.rest.client.types.LoginRequest;
import org.junit.After;
import org.junit.Before;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserGroupControllerClientTest extends LCMBaseServerTest {

    @Autowired
    private UserGroupService userGroupService;

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
    public void testGetUserGroupsWebTarget() throws LoginException {
        Response res = target
                .path("client/v0/userGroups")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .get();

        assertEquals(200, res.getStatus());
    }

    @Test
    public void testGetUserGroupWebTarget() throws ServerException, LoginException {

        UserGroupDao userGroupDao = userGroupService.getUserGroupDao();
        UserGroup userGroup = new UserGroup();
        userGroup.setId("admin");
        userGroupDao.persist(userGroup);

        Response res1 = target
                .path("client/v0/userGroups/"+userGroup.getId())
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .get();

        assertEquals(200, res1.getStatus());
    }

    @Test
    public void testSaveUserGroupWebTarget() throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException {
        List<String> users = new ArrayList();
        users.add("admin");

        UserGroup userGroup = new UserGroup();
        userGroup.setId("testUserGroup");
        userGroup.setUsers(users);

        Entity<UserGroup> entity = Entity.entity(userGroup, "application/nl.kpmg.lcm.server.data.UserGroup+json");
        Response res1 = target
                .path("client/v0/userGroups/testUserGroup")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .put(entity);

        assertEquals(200, res1.getStatus());
    }

    @Test
    public void testModifyUserGroupWebTarget() throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException {
        List<String> users = new ArrayList();
        users.add("admin");

        UserGroup userGroup = new UserGroup();
        userGroup.setId("testUserGroup");
        userGroup.setUsers(users);

        Entity<UserGroup> entity = Entity.entity(userGroup, "application/nl.kpmg.lcm.server.data.UserGroup+json");
        Response res1 = target
                .path("client/v0/userGroups/testUserGroup")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .put(entity);

        assertEquals(200, res1.getStatus());
    }

    @Test
    public void testDeleteUserGroupWebTarget() throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException {
         List<String> users = new ArrayList();
        users.add("admin");

        UserGroup userGroup = new UserGroup();
        userGroup.setId("testUserGroup1");
        userGroup.setUsers(users);

        Entity<UserGroup> entity1 = Entity.entity(userGroup, "application/nl.kpmg.lcm.server.data.UserGroup+json");
        Response res1 = target
                .path("client/v0/userGroups")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .post(entity1);

        Response res2 = target
                .path("client/v0/userGroups/testUserGroup1")
                .request()
                .header("LCM-Authentication-User", "admin")
                .header("LCM-Authentication-Token", authenticationToken)
                .delete();

        assertEquals(200, res1.getStatus());
        assertEquals(200, res2.getStatus());
    }
}
