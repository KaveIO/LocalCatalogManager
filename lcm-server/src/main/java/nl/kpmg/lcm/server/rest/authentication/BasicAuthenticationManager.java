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

import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.BASIC_AUTHENTICATION_HEADER;
import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.LCM_AUTHENTICATION_ORIGIN_HEADER;
import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.LCM_AUTHENTICATION_REMOTE_USER_HEADER;

import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.data.service.UserService;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.container.ContainerRequestContext;

/**
 * Authentication Manager.
 *
 * @author mhoekstra
 */
@Service
public class BasicAuthenticationManager extends AbstractAuthenticationManager {

  private Boolean isBasicAuthenticationEnabled;

  @Value("${lcm.server.basic.authentication.enabled}")
  public final void setIsBasicAuthenticationEnabled(final String isEnabled) {
    this.isBasicAuthenticationEnabled = Boolean.valueOf(isEnabled);
  }

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthenticationManager.class);

  @Autowired
  public BasicAuthenticationManager(UserService userService) {
    super(userService);
  }

  @Override
  public boolean isEnabled() {
    return isBasicAuthenticationEnabled;
  }

  @Override
  public boolean isAuthenticationValid(ContainerRequestContext requestContext) {
    String requestOrigin  = requestContext.getHeaderString(LCM_AUTHENTICATION_ORIGIN_HEADER);
    if (requestOrigin == null) {
      LOGGER.info("Unable to read origin");
      return false;
    }

    String authenticationString = requestContext.getHeaderString(BASIC_AUTHENTICATION_HEADER);
    Credentials credentials = authenticationStringToCredentials(authenticationString);

    if (credentials != null) {
      return isUsernamePasswordValid(requestOrigin, credentials.getUsername(), credentials.getPassword());
    }

    return false;
  }

  @Override
  public UserSecurityContext getSecurityContext(ContainerRequestContext requestContext) {
    String authenticationString = requestContext.getHeaderString(BASIC_AUTHENTICATION_HEADER);
    String remoteUser = requestContext.getHeaderString(LCM_AUTHENTICATION_REMOTE_USER_HEADER);
    String requestOrigin  = requestContext.getHeaderString(LCM_AUTHENTICATION_ORIGIN_HEADER);
    Credentials credentials = authenticationStringToCredentials(authenticationString);
    if (credentials == null) {
      LOGGER.warn("Unable to parse credentials for authentication string: " + authenticationString);
      return null;
    }
    try {
      // in case this is request for another LCM the the credentials are LCM based and they auhtorize the
      // LCM, the actual user who is using the remote LCM is passed as header.
      String username = requestOrigin.equals(User.LOCAL_ORIGIN)? credentials.getUsername(): remoteUser;
      Session session = createSessionForUser(requestOrigin, username);
      LOGGER.debug("Successfully created session for user: " + username);
      return new UserSecurityContext(session);
    } catch (LoginException ex) {
      LOGGER.error(ex.getMessage(), ex);
    }
    return null;
  }

  private Credentials authenticationStringToCredentials(String authenticationString) {

    if (authenticationString == null) {
      LOGGER.info("Authentication string is null can not be parsed");
      return null;
    }
    // header value format will be "Basic encodedstring" for Basic
    // authentication. Example "Basic YWRtaW46YWRtaW4="
    final String encodedUserPassword = authenticationString.replaceFirst("Basic" + " ", "");
    String usernameAndPassword = null;

    byte[] decodedBytes = Base64.decodeBase64(encodedUserPassword.getBytes());
    usernameAndPassword = new String(decodedBytes);

    String[] split = StringUtils.split(usernameAndPassword, ":", 2);

    if (split.length == 2 && !split[0].isEmpty() && !split[1].isEmpty()) {

      return new Credentials(split[0], split[1]);
    }
    
    LOGGER.info("Authentication string hass incorrect format, can not be parsed " + authenticationString);
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
