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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

/**
 * A SecurityContext based on the Session class.
 */
public final class UserSecurityContext implements SecurityContext {
  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(UserSecurityContext.class);

  /**
   * The principal user.
   */
  private final Session session;

  /**
   * Creates a security context based on a User Object.
   *
   * @param session the session to use in this context
   */
  public UserSecurityContext(final Session session) {
    this.session = session;
  }

  @Override
  public Principal getUserPrincipal() {
    return new Principal() {
      @Override
      public String getName() {
        return session.getUsername();
      }
    };
  }

  @Override
  public boolean isUserInRole(final String role) {
    LOGGER
        .info(String.format("LCMRESTRequestFilter::LCMSecurityContext called with role %s", role));
    return session.getRole().equals(role);
  }

  @Override
  public boolean isSecure() {
    return false;
  }

  @Override
  public String getAuthenticationScheme() {
    return null;
  }
}
