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

import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.LCM_AUTHENTICATION_ORIGIN_HEADER;
import static org.junit.Assert.assertEquals;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.data.UserGroup;
import nl.kpmg.lcm.server.LcmBaseServerTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.data.dao.UserGroupDao;
import nl.kpmg.lcm.server.data.service.UserGroupService;
import nl.kpmg.lcm.server.rest.client.types.LoginRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class UserGroupControllerClientTest extends LcmBaseServerTest {

  @Autowired
  private UserGroupService userGroupService;

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
    Response res = getWebTarget().path("client/login").request()
            .header(LCM_AUTHENTICATION_ORIGIN_HEADER, User.LOCAL_ORIGIN)
            .post(entity);
    authenticationToken = res.readEntity(String.class);
  }

  @After
  public void afterTest() throws ServerException {
    getWebTarget().path("client/logout").request()
            .header(LCM_AUTHENTICATION_ORIGIN_HEADER, User.LOCAL_ORIGIN)
            .header("LCM-Authentication-User", "admin")
        .header("LCM-Authentication-Token", authenticationToken).post(null);
  }

  @Test
  public void testGetUserGroupsWebTarget() throws LoginException, ServerException {
    Response res = getWebTarget().path("client/v0/userGroups").request()
        .header("LCM-Authentication-User", "admin")
        .header("LCM-Authentication-Token", authenticationToken).get();

    assertEquals(200, res.getStatus());
  }

  @Test
  public void testGetUserGroupWebTarget() throws ServerException, LoginException {

    UserGroupDao userGroupDao = userGroupService.getUserGroupDao();
    UserGroup userGroup = new UserGroup();
    userGroup.setName("admin");
    UserGroup saved = userGroupDao.save(userGroup);

    Response res1 = getWebTarget().path("client/v0/userGroups/" + saved.getId()).request()
            .header(LCM_AUTHENTICATION_ORIGIN_HEADER, User.LOCAL_ORIGIN)
        .header("LCM-Authentication-User", "admin")
        .header("LCM-Authentication-Token", authenticationToken).get();

    assertEquals(200, res1.getStatus());
  }

  @Test
  public void testSaveUserGroupWebTarget()
      throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException, ServerException {
    List<String> users = new ArrayList();
    users.add("admin");

    UserGroup userGroup = new UserGroup();
    userGroup.setId("testUserGroup");
    userGroup.setUsers(users);

    Entity<UserGroup> entity =
        Entity.entity(userGroup, "application/nl.kpmg.lcm.server.data.UserGroup+json");
    Response res1 = getWebTarget().path("client/v0/userGroups/").request()
            .header(LCM_AUTHENTICATION_ORIGIN_HEADER, User.LOCAL_ORIGIN)
        .header("LCM-Authentication-User", "admin")
        .header("LCM-Authentication-Token", authenticationToken).post(entity);

    assertEquals(200, res1.getStatus());
  }

  @Test
  public void testModifyUserGroupWebTarget()
      throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException, ServerException {
    List<String> users = new ArrayList();
    users.add("admin");

    UserGroup userGroup = new UserGroup();
    userGroup.setId("testUserGroup");
    userGroup.setUsers(users);

    Entity<UserGroup> entity =
        Entity.entity(userGroup, "application/nl.kpmg.lcm.server.data.UserGroup+json");
    Response res1 = getWebTarget().path("client/v0/userGroups/").request()
            .header(LCM_AUTHENTICATION_ORIGIN_HEADER, User.LOCAL_ORIGIN)
        .header("LCM-Authentication-User", "admin")
        .header("LCM-Authentication-Token", authenticationToken).put(entity);

    assertEquals(200, res1.getStatus());
  }

  @Test
  public void testDeleteUserGroupWebTarget()
      throws LoginException, NoSuchAlgorithmException, InvalidKeySpecException, ServerException {
    List<String> users = new ArrayList();
    users.add("admin");

    UserGroup userGroup = new UserGroup();
    userGroup.setId("testUserGroup1");
    userGroup.setUsers(users);

    Entity<UserGroup> entity1 =
        Entity.entity(userGroup, "application/nl.kpmg.lcm.server.data.UserGroup+json");
    Response res1 = getWebTarget().path("client/v0/userGroups").request()
            .header(LCM_AUTHENTICATION_ORIGIN_HEADER, User.LOCAL_ORIGIN)
        .header("LCM-Authentication-User", "admin")
        .header("LCM-Authentication-Token", authenticationToken).post(entity1);

    Response res2 = getWebTarget().path("client/v0/userGroups/testUserGroup1").request()
            .header(LCM_AUTHENTICATION_ORIGIN_HEADER, User.LOCAL_ORIGIN)
        .header("LCM-Authentication-User", "admin")
        .header("LCM-Authentication-Token", authenticationToken).delete();

    assertEquals(200, res1.getStatus());
    assertEquals(200, res2.getStatus());
  }
}
