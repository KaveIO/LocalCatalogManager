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
package nl.kpmg.lcm.server.rest.authentication;

import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.rest.authentication.UserPasswordHashException;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.data.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author mhoekstra
 */
public abstract class AbstractAuthenticationManager implements AuthenticationManager {
  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAuthenticationManager.class);

  /**
   * The user service.
   */
  private final UserService userService;

  /**
   * the hard admin username provided by the properties file.
   */
  private String adminUser;

  /**
   * the hard admin password provided by the properties file.
   */
  private String adminPassword;


  @Autowired
  public AbstractAuthenticationManager(UserService userService) {
    this.userService = userService;
  }

  @Value("${lcm.server.adminUser}")
  public final void setAdminUser(final String adminUser) {
    this.adminUser = adminUser;
  }

  @Value("${lcm.server.adminPassword}")
  public final void setAdminPassword(final String adminPassword) {
    this.adminPassword = adminPassword;
  }

  protected boolean isUsernamePasswordValid(String username, final String password) {
    //TODO refactor this when implementing the auhtentication
    username  =  (username.split("@"))[0];
    if (username.equals(adminUser)) {
      LOGGER.info("Caught login attempt for admin user");
      if (password.equals(adminPassword)) {
        return true;
      }
    } else {
      LOGGER.info("Caught login attempt for regular user");
      User user = userService.findOneByName(username);
      try {
        if (user != null && user.passwordEquals(password)) {
          return true;
        }
      } catch (UserPasswordHashException ex) {
        LOGGER.error("Something went wrong with the password hashing algorithm", ex);
      }
    }
    return false;
  }

  protected Session createSessionForUser(String complexUsername) throws LoginException {
    String username = complexUsername;
    String remoteLcmUID = null;
    //TODO there must be more secure way to determinate remote users!
    //If the remote user does not add '@LCM_ID' then it will be treate as local user
    // and will have permissions as local
    if (complexUsername.contains("@")) {
      String[] splitted = complexUsername.split("@");
      username = splitted[0];
      remoteLcmUID = splitted[1];
    }

    if (remoteLcmUID !=  null && !remoteLcmUID.equals(User.LOCAL_ORIGIN) && username.equals(adminUser)) {
      throw new LoginException("Remote user is trying to login "
          + "with preconfiguredadmin user! User: " + username + " and lcmUID: " + remoteLcmUID);
    }

    if (username.equals(adminUser)) {
      return new Session(username, Roles.ADMINISTRATOR, UserOrigin.CONFIGURED, remoteLcmUID);
    } else {
      User user = userService.findOneByName(username);
      String role = remoteLcmUID != null ? user.getRole() : Roles.REMOTE_USER;
      if (user != null) {
        return new Session(user.getName(), role, UserOrigin.LOCAL, remoteLcmUID);
      }
    }
    throw new LoginException("Session could not be constructed after login.");
  }
}
