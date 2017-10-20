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

import nl.kpmg.lcm.common.data.AuthorizedLcm;
import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.rest.authentication.UserPasswordHashException;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.data.service.AuthorizedLcmService;
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

  @Autowired
  private AuthorizedLcmService authorizedLcmService;

  /**
   * the hard admin username provided by the properties file.
   */
  private String hardcodedAdminUser;

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
    this.hardcodedAdminUser = adminUser;
  }

  @Value("${lcm.server.adminPassword}")
  public final void setAdminPassword(final String adminPassword) {
    this.adminPassword = adminPassword;
  }

  protected boolean isUsernamePasswordValid(String origin, String username, final String password) {
    if(origin == null || username ==  null || password == null){
        LOGGER.info("Trying to authenticate invalid user! Some of the parameters are null.");
        return false;
    }

    if(origin.equals(User.LOCAL_ORIGIN)){
        if (hardcodedAdminUser !=  null && hardcodedAdminUser.length()> 0
                && username.equals(hardcodedAdminUser)) {
          LOGGER.info("Caught login attempt for admin user");
          if (adminPassword !=  null && password.length()> 0
                  && password.equals(adminPassword)) {
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
    } else {
        AuthorizedLcm lcm = authorizedLcmService.findOneByUniqueId(origin);
        if(lcm ==  null) {
            LOGGER.info("Request is not authenticated. Lcm with id : " + origin + " is not found!");
            return false;
        }

        if(lcm.getApplicationId().equals(username) && lcm.getApplicationKey().equals(password)){
            LOGGER.info("Request is authenticated sucessfully. Lcm id : " + origin);
            return true;
        }

        LOGGER.info("Request is not authenticated. Lcm with id : " + origin + " is found,  but credentials are wrong! Passed  applicationId: " + username);
        return false;

    }
    return false;
  }

  protected Session createSessionForUser(String origin, String username) throws LoginException {
     if(origin ==  null) {
        throw new LoginException("Session could not be constructed  for user with unknow origin.");
     }

     if(!origin.equals(User.LOCAL_ORIGIN)){
        return new Session(username, Roles.REMOTE_USER, UserOrigin.LOCAL, origin);
     }

    //the origin is local
    if (username.equals(hardcodedAdminUser)) {
      return new Session(username, Roles.ADMINISTRATOR, UserOrigin.CONFIGURED, origin);
    } else {
      User user = userService.findOneByName(username);
      if (user != null) {
        return new Session(user.getName(), user.getRole(), UserOrigin.LOCAL, origin);
      }
    }
    throw new LoginException("Session could not be constructed after login.");
  }
}
