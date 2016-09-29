/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package nl.kpmg.lcm.server.rest.client.version0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import nl.kpmg.lcm.rest.types.UserRepresentation;
import nl.kpmg.lcm.rest.types.UsersRepresentation;
import nl.kpmg.lcm.server.LcmBaseServerTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.dao.UserDao;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.authentication.SessionAuthenticationManager;
import nl.kpmg.lcm.server.rest.authentication.UserPasswordHashException;
import nl.kpmg.lcm.server.rest.client.types.LoginRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class UserControllerClientTest extends LcmBaseServerTest {

  @Autowired
  private UserService userService;

  @Autowired
  private SessionAuthenticationManager am;

  private String authenticationToken;

  @Before
  public void beforeTest() throws ServerException {
    // Due to a Spring configuration error we can't login in this thread. We
    // have to create a actuall login call.
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setUsername("admin");
    loginRequest.setPassword("admin");
    Entity<LoginRequest> entity = Entity.entity(loginRequest,
        "application/nl.kpmg.lcm.server.rest.client.types.LoginRequest+json");
    Response res = getWebTarget().path("client/login").request().post(entity);
    authenticationToken = res.readEntity(String.class);
  }

  @After
  public void afterTest() throws ServerException {
    getWebTarget().path("client/logout").request().header("LCM-Authentication-User", "admin")
        .header("LCM-Authentication-Token", authenticationToken).post(null);

    UserDao userDao = userService.getUserDao();
    for (User user : userDao.findAll()) {
      userDao.delete(user);
    }
  }

  @Test
  public void testGetUserTarget()
      throws LoginException, UserPasswordHashException, ServerException {
    UserDao userDao = userService.getUserDao();
    User expected = new User();
    expected.setName("testUser");
    expected.setPassword("testPassword");

    User saved = userDao.save(expected);

    Response response = getWebTarget().path("client/v0/users/" + saved.getId()).request()
        .header("LCM-Authentication-User", "admin")
        .header("LCM-Authentication-Token", authenticationToken).get();

    assertEquals(200, response.getStatus());

    User actual = response.readEntity(UserRepresentation.class).getItem();

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getRole(), actual.getRole());
    assertNull("The password should NEVER be returned via the interface", actual.getPassword());
  }

  @Test
  public void testGetUsersWebTarget() throws LoginException, ServerException {
    UserDao userDao = userService.getUserDao();

    User user = new User();
    user.setName("user");
    user.setNewPassword("password");
    userDao.save(user);

    Response res1 =
        getWebTarget().path("client/v0/users").request().header("LCM-Authentication-User", "admin")
            .header("LCM-Authentication-Token", authenticationToken).get();

    assertEquals(200, res1.getStatus());


    UsersRepresentation users = res1.readEntity(UsersRepresentation.class);

    List<UserRepresentation> items = users.getItems();
    User actual = items.get(0).getItem();

    assertNull("The password should NEVER be returned via the interface", actual.getPassword());
  }

  @Test
  public void testSaveUserWebTarget()
      throws LoginException, UserPasswordHashException, ServerException {
    User user = new User();
    user.setName("admin");
    user.setNewPassword("admin");
    Entity<User> entity = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");
    Response res1 =
        getWebTarget().path("client/v0/users").request().header("LCM-Authentication-User", "admin")
            .header("LCM-Authentication-Token", authenticationToken).post(entity);

    assertEquals(200, res1.getStatus());
  }

  @Test
  public void testModifyUserWebTarget()
      throws LoginException, UserPasswordHashException, ServerException {
    UserDao userDao = userService.getUserDao();

    User user = new User();
    user.setName("admin123");
    user.setPassword("admin");
    User saved = userDao.save(user);

    saved.setNewPassword("admin123");

    Response res1 = getWebTarget().path("client/v0/users/" + saved.getId()).request()
        .header("LCM-Authentication-User", "admin")
        .header("LCM-Authentication-Token", authenticationToken)
        .put(Entity.entity(saved, "application/nl.kpmg.lcm.server.data.User+json"));

    assertEquals(200, res1.getStatus());

    User actual = userDao.findOneByName("admin123");
    assertTrue(actual.passwordEquals("admin123"));
  }

  @Test
  public void testDeleteUserWebTarget()
      throws LoginException, UserPasswordHashException, ServerException {
    UserDao userDao = userService.getUserDao();
    assertNull(userDao.findOneByName("admin123"));

    User user = new User();
    user.setName("admin123");
    user.setPassword("admin");
    User saved = userDao.save(user);

    Response response;

    // Delete the user
    response = getWebTarget().path("client/v0/users/" + saved.getId()).request()
        .header("LCM-Authentication-User", "admin")
        .header("LCM-Authentication-Token", authenticationToken).delete();
    assertEquals(200, response.getStatus());

    assertNull(userDao.findOne(saved.getId()));
    assertNull(userDao.findOneByName(saved.getName()));
  }

  @Test
  public void testLogoutUser() throws LoginException, UserPasswordHashException, ServerException {
    User user = new User();
    user.setName("admin");
    user.setPassword("admin");
    Entity<User> entity = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");

    Response res1 =
        getWebTarget().path("client/logout").request().header("LCM-Authentication-User", "admin")
            .header("LCM-Authentication-Token", authenticationToken).post(entity);

    assertEquals(200, res1.getStatus());
  }
}
