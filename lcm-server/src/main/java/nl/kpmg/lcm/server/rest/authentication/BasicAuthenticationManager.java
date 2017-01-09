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

package nl.kpmg.lcm.server.rest.authentication;

import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.data.service.UserService;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.ws.rs.container.ContainerRequestContext;

/**
 * Authentication Manager.
 *
 * @author mhoekstra
 */
public class BasicAuthenticationManager extends AuthenticationManager {

  /**
   * The name of the http request header containing the authentication user.
   */
  public static final String BASIC_AUTHENTICATION_HEADER = "Authorization";

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthenticationManager.class);

  /**
   * the hard admin username provided by the properties file.
   */
  private String adminUser;

  /**
   * the hard admin password provided by the properties file.
   */
  private String adminPassword;

  /**
   * The map containing all users that have been properly authenticated.
   */
  private final Map<String, Session> authenticationTokenMap = new HashMap();

  @Autowired
  public BasicAuthenticationManager(UserService userService) {
    super(userService);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean isAuthenticationValid(ContainerRequestContext requestContext) {
    String authenticationString = requestContext.getHeaderString(BASIC_AUTHENTICATION_HEADER);
    Credentials credentials = authenticationStringToCredentials(authenticationString);

    if (credentials != null) {
      return isUsernamePasswordValid(credentials.getUsername(), credentials.getPassword());
    }
    return false;
  }

  @Override
  public UserSecurityContext getSecurityContext(ContainerRequestContext requestContext) {
    String authenticationString = requestContext.getHeaderString(BASIC_AUTHENTICATION_HEADER);
    Credentials credentials = authenticationStringToCredentials(authenticationString);

    try {
      Session session = createSessionForUser(credentials.getUsername());
      return new UserSecurityContext(session);
    } catch (LoginException ex) {
      java.util.logging.Logger.getLogger(BasicAuthenticationManager.class.getName())
          .log(Level.SEVERE, null, ex);
    }
    return null;
  }

  private Credentials authenticationStringToCredentials(String authenticationString) {

    if (authenticationString == null) {
      return null;
    }
    // header value format will be "Basic encodedstring" for Basic
    // authentication. Example "Basic YWRtaW46YWRtaW4="
    final String encodedUserPassword = authenticationString.replaceFirst("Basic" + " ", "");
    String usernameAndPassword = null;

    byte[] decodedBytes = Base64.decodeBase64(encodedUserPassword.getBytes());
    usernameAndPassword = new String(decodedBytes);

    String[] split = usernameAndPassword.split(":");

    if (split.length == 2 && !split[0].isEmpty() && !split[1].isEmpty()) {

      return new Credentials(split[0], split[1]);
    }
    return null;
  }

  private class Credentials {

    private final String username;
    private final String password;

    public Credentials(String username, String password) {
      this.username = username;
      this.password = password;
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }
  }
}
