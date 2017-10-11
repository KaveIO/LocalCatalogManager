/*
 * Copyright 2016 KPMG N.V. (unless otherwise stated).
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
package nl.kpmg.lcm.server.rest.authorization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.data.UserGroup;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.LcmBaseServerTest;
import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.data.service.UserGroupService;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.server.rest.authentication.Session;
import nl.kpmg.lcm.server.rest.authentication.UserOrigin;
import nl.kpmg.lcm.server.rest.authentication.UserSecurityContext;
import nl.kpmg.lcm.server.test.mock.MetaDataMocker;
import nl.kpmg.lcm.server.test.mock.UserMocker;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shristov
 */
public class PermissionCheckerTest extends LcmBaseServerTest {

  @Autowired
  private UserService userService;

  @Autowired
  private UserGroupService userGroupService;

  @Autowired
  private MetaDataService metaDataService;

  @Autowired
  private PermissionChecker simpleChecker;

  private static ExternalAuthorizationServiceMock authService =
      new ExternalAuthorizationServiceMock();
  private static PermissionChecker externalChecker = new PermissionChecker(authService);

  @Before
  public void setUp() {
    userService.removeAll();
    metaDataService.removeAll();
  }

  @Test
  public void testSimpleCheck() {
    Session session = new Session("admin", Roles.ADMINISTRATOR, UserOrigin.LOCAL, null);
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result =
        simpleChecker.check(securityContext, new String[] {Roles.ADMINISTRATOR, Roles.API_USER});
    assertTrue(result);
  }

  @Test
  public void testSimpleCheckUnauthorized() {
    Session session = new Session("admin", Roles.ADMINISTRATOR, UserOrigin.LOCAL, null);
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result =
        simpleChecker.check(securityContext, new String[] {Roles.REMOTE_USER, Roles.API_USER});
    assertFalse(result);
  }

  @Test
  public void testExternalCheck() {
    Session session = new Session("admin", Roles.ADMINISTRATOR, UserOrigin.LOCAL, null);
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result =
        externalChecker.check(securityContext, new String[] {Roles.ADMINISTRATOR, Roles.API_USER});
    assertTrue(result);
  }

  @Test
  public void testExternalCheckUnauthorized() {
    Session session = new Session("admin", Roles.ADMINISTRATOR, UserOrigin.LOCAL, null);
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result =
        externalChecker.check(securityContext, new String[] {Roles.ADMINISTRATOR, Roles.API_USER});
    assertFalse(result);
  }

  @Test
  public void testExternalCheckUnauthorizedMissingResourceId() {
    Session session = new Session("admin", Roles.ADMINISTRATOR, UserOrigin.LOCAL, null);
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result =
        externalChecker.check(securityContext, new String[] {Roles.REMOTE_USER, Roles.API_USER});
    assertFalse(result);
  }

  @Test
  public void testExternalCheckResourceId() {
    Session session = new Session("admin", Roles.ADMINISTRATOR, UserOrigin.LOCAL, null);
    UserSecurityContext securityContext = new UserSecurityContext(session);

    String resourceName = PermissionCheckerTest.class.getName() + ".testExternalCheck";
    boolean result =
        externalChecker.check(securityContext, resourceName, new String[] {Roles.ADMINISTRATOR,
            Roles.API_USER});
    assertTrue(result);
  }

  @Test
  public void testExternalCheckWithResourceIdUnauthorized() {
    Session session = new Session("admin", Roles.ADMINISTRATOR, UserOrigin.LOCAL, null);
    UserSecurityContext securityContext = new UserSecurityContext(session);

    String resourceName = PermissionCheckerTest.class.getName() + ".testExternalCheckUnauthorized";
    boolean result =
        externalChecker.check(securityContext, resourceName, new String[] {Roles.ADMINISTRATOR,
            Roles.API_USER});
    assertFalse(result);
  }

  /**
   * Test case in which the metadata is not added to the allowed list
   */
  @Test
  public void testUnautorizedAccess() {
    MetaData metaData = MetaDataMocker.getMetaData();
    metaDataService.create(metaData);
    User user = UserMocker.createUnauthorizedRemoteUser();
    userService.save(user);

    Session session =
        new Session(user.getName(), user.getRole(), UserOrigin.LOCAL, user.getOrigin());
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result = simpleChecker.check(securityContext, metaData.getId());
    assertFalse(result);
  }


  /**
   * Test case in which the metadata is not added to the allowed list but the list exists and other
   * metadatas are added.
   */
  @Test
  public void testUnautorizedAccess2() {
    MetaData metaData = MetaDataMocker.getMetaData();
    metaDataService.create(metaData);

    MetaData metaData2 = MetaDataMocker.getMetaData();
    metaData2.setId("598857877b0c2518a02592c9");
    metaDataService.create(metaData2);

    User user = UserMocker.createUnauthorizedRemoteUser();
    List permittedMetadataList = new ArrayList<String>();
    permittedMetadataList.add(metaData2.getId());
    user.setAllowedMetadataList(permittedMetadataList);
    userService.save(user);

    Session session =
        new Session(user.getName(), user.getRole(), UserOrigin.LOCAL, user.getOrigin());
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result = simpleChecker.check(securityContext, metaData.getId());
    assertFalse(result);
  }

  /**
   * Test case in which the metadata is not added to the allowed list but the path list exists.
   */
  @Test
  public void testUnautorizedAccess3() {
    MetaData metaData = MetaDataMocker.getMetaData();
    metaDataService.create(metaData);

    User user = UserMocker.createUnauthorizedRemoteUser();
    List permittedPathList = new ArrayList<String>();
    permittedPathList.add("not-existing-path");
    user.setAllowedPathList(permittedPathList);
    userService.save(user);

    Session session =
        new Session(user.getName(), user.getRole(), UserOrigin.LOCAL, user.getOrigin());
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result = simpleChecker.check(securityContext, metaData.getId());
    assertFalse(result);
  }

  /**
   * Test case in which the metadata is not added to the allowed list but the path list exists.
   */
  @Test
  public void testDirectlyPathAutorizedAccess() {

    MetaData metaData = MetaDataMocker.getMetaData();
    MetaDataWrapper wrapper = new MetaDataWrapper(metaData);
    wrapper.getData().setPath("test1");
    metaDataService.create(wrapper.getMetaData());

    User user = UserMocker.createUnauthorizedRemoteUser();
    user.setName("pathAuthorizedUser");
    List permittedPathList = new ArrayList<String>();
    permittedPathList.add("test1");
    user.setAllowedPathList(permittedPathList);
    user = userService.save(user);

    Session session =
        new Session(user.getName(), user.getRole(), UserOrigin.LOCAL, user.getOrigin());
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result = simpleChecker.check(securityContext, metaData.getId());
    assertTrue(result);
  }

  /**
   * Test case in which the user is authorized to access metadata A.
   * Successful access to metadata A is tested.
   */
  @Test
  public void testDirectlyAutorizedAccess() {
    MetaData metaData = MetaDataMocker.getMetaData();
    MetaDataWrapper wrapper = new MetaDataWrapper(metaData);
    wrapper.getData().setPath("test1");
    metaDataService.create(wrapper.getMetaData());

    User user = UserMocker.createUnauthorizedRemoteUser();
    user.setName("authorizedUser");
    List permittedMetadataList = new ArrayList<String>();
    permittedMetadataList.add(metaData.getId());
    user.setAllowedMetadataList(permittedMetadataList);
    user = userService.save(user);

    Session session =
        new Session(user.getName(), user.getRole(), UserOrigin.LOCAL, user.getOrigin());
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result = simpleChecker.check(securityContext, metaData.getId());
    assertTrue(result);
  }
  
  
  /**
   * Test case in which the user is admin. The admin user
   * can access all the metadata.
   */
  @Test
  public void testAdminAccess() {
    MetaData metaData = MetaDataMocker.getMetaData();
    MetaDataWrapper wrapper = new MetaDataWrapper(metaData);
    metaDataService.create(wrapper.getMetaData());

    User user = UserMocker.createAdminUser();
    user.setName("adminUser");
    user = userService.save(user);

    Session session =
        new Session(user.getName(), user.getRole(), UserOrigin.LOCAL, user.getOrigin());
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result = simpleChecker.check(securityContext, metaData.getId());
    assertTrue(result);
  }

  /**
   * Test case in which the user is  part of usergroup  and the usergroup is permitted 
   * to access path A. Successful access to metadata  from the path A is tested.
   */
  @Test
  public void testGroupPathAutorizedAccess() {

    MetaData metaData = MetaDataMocker.getMetaData();
    MetaDataWrapper wrapper = new MetaDataWrapper(metaData);
    wrapper.getData().setPath("groupPath");
    metaDataService.create(wrapper.getMetaData());

    User user = UserMocker.createUnauthorizedRemoteUser();
    user.setName("groupPathAuthorizedUser");
    user = userService.save(user);
        UserGroup userGroup = new UserGroup();
    userGroup.addUser(user.getId());
    List permittedPathList = new ArrayList<String>();
    permittedPathList.add("groupPath");
    userGroup.setAllowedPathList(permittedPathList);
    userGroupService.save(userGroup);

    Session session =
        new Session(user.getName(), user.getRole(), UserOrigin.LOCAL, user.getOrigin());
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result = simpleChecker.check(securityContext, metaData.getId());
    assertTrue(result);
  }

   /**
   * Test case in which the user is  part of usergroup  and the usergroup is permitted
   * to access path A. Unsuccessful access to metadata from path B is tested.
   */
  @Test
  public void testUnauthorizedGroupPathAccess() {

    MetaData metaData = MetaDataMocker.getMetaData();
    MetaDataWrapper wrapper = new MetaDataWrapper(metaData);
    wrapper.getData().setPath("test1");
    metaDataService.create(wrapper.getMetaData());

    User user = UserMocker.createUnauthorizedRemoteUser();
    user.setName("groupPathAuthorizedUser");
    user = userService.save(user);
    UserGroup userGroup = new UserGroup();
    userGroup.addUser(user.getId());
    List permittedPathList = new ArrayList<String>();
    permittedPathList.add("groupPath");
    userGroup.setAllowedPathList(permittedPathList);
    userGroupService.save(userGroup);    

    Session session =
        new Session(user.getName(), user.getRole(), UserOrigin.LOCAL, user.getOrigin());
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result = simpleChecker.check(securityContext, metaData.getId());
    assertFalse(result);
  }

   /**
   * Test case in which the user is  part of usergroup  and the usergroup is permitted 
   * to access metadata A. Successful access to metadata A is tested.
   */
  @Test
  public void testGroupAuthorizedAccess() {
    MetaData metaData = MetaDataMocker.getMetaData();
    MetaDataWrapper wrapper = new MetaDataWrapper(metaData);
    wrapper.getData().setPath("test1");
    metaDataService.create(wrapper.getMetaData());

    User user = UserMocker.createUnauthorizedRemoteUser();
    user.setName("authorizedGroupUser");
    user = userService.save(user);
    UserGroup userGroup = new UserGroup();
    userGroup.addUser(user.getId());
    List permittedMetadataList = new ArrayList<String>();
    permittedMetadataList.add(metaData.getId());
    userGroup.setAllowedMetadataList(permittedMetadataList);
    
    userGroupService.save(userGroup);

    Session session =
        new Session(user.getName(), user.getRole(), UserOrigin.LOCAL, user.getOrigin());
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result = simpleChecker.check(securityContext, metaData.getId());
    assertTrue(result);
  }
  
   /**
   * Test case in which the user is  part of usergroup  and the usergroup is permitted 
   * to access metadata A . Unsuccessful access to the metadata B is tested.
   */
  @Test
  public void testUnautorizedGroupAccess() {
    MetaData metaData = MetaDataMocker.getMetaData();
    MetaDataWrapper wrapper = new MetaDataWrapper(metaData);
    metaDataService.create(wrapper.getMetaData());

    User user = UserMocker.createUnauthorizedRemoteUser();
    user.setName("authorizedGroupUser");
    user = userService.save(user);
    UserGroup userGroup = new UserGroup();
    userGroup.addUser(user.getId());
    
    MetaData metaData2 = MetaDataMocker.getMetaData();
    metaData2.setId("598857877b0c2518a02592c9");
    metaDataService.create(metaData2);
    List permittedMetadataList = new ArrayList<String>();
    permittedMetadataList.add(metaData2.getId());
    userGroup.setAllowedMetadataList(permittedMetadataList);
    
    userGroupService.save(userGroup);

    Session session =
        new Session(user.getName(), user.getRole(), UserOrigin.LOCAL, user.getOrigin());
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result = simpleChecker.check(securityContext, metaData.getId());
    assertFalse(result);
  }
}