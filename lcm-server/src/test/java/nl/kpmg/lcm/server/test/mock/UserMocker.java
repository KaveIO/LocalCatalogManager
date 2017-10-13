/*
 * Copyright 2017 KPMG N.V. (unless otherwise stated).
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
package nl.kpmg.lcm.server.test.mock;

import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.rest.authentication.UserPasswordHashException;
import nl.kpmg.lcm.server.rest.authentication.Roles;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shristov
 */
public class UserMocker {

  public static User createAdminUser() {
    User user = new User();
    user.setName("admin-user");
    user.setRole(Roles.ADMINISTRATOR);
    try {
      user.setPassword("testPassword");
    } catch (UserPasswordHashException ex) {
      return null;
    }

    user.setAllowedMetadataList(new ArrayList<String>());
    user.setAllowedPathList(new ArrayList<String>());

    return user;
  }

  public static User createRemoteUser(List<String> metadataList, List<String> pathList) {
    User user = new User();
    user.setName("remote-user");
    user.setRole(Roles.REMOTE_USER);
    try {
      user.setPassword("testPassword");
    } catch (UserPasswordHashException ex) {
      return null;
    }
    user.setAllowedMetadataList(metadataList);
    user.setAllowedPathList(pathList);

    return user;
  }

  public static User createUnauthorizedRemoteUser() {
    return createRemoteUser(new ArrayList<String>(), new ArrayList<String>());
  }

  public static User createRemoteDirectlyAuthorizedUser(List<String> metadataList) {
    return createRemoteUser(metadataList, new ArrayList<String>());
  }

  public static User createRemoteUserAuthorizedByPath(List<String> pathList) {
    return createRemoteUser(new ArrayList<String>(), pathList);
  }
}
