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

import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.server.rest.authentication.UserSecurityContext;

import org.springframework.stereotype.Service;

import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author shristov
 */
@Service
public class PermissionChecker {
  private AuthorizationService authorizationService;

  public PermissionChecker() {}

  public PermissionChecker(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  /**
   *
   * @param securityContext
   * @param defaultRoles
   * @return true if the principal specified in @securityContext has rights to access calling method
   */

  public boolean check(SecurityContext securityContext, String[] defaultRoles) {
    String callerMethodName =
        Thread.currentThread().getStackTrace()[2].getClassName() + "."
            + Thread.currentThread().getStackTrace()[2].getMethodName();

    return check(securityContext, callerMethodName, defaultRoles);
  }

  public boolean check(SecurityContext securityContext, String resourceId, String[] defaultRoles) {
    if (authorizationService != null) {
      if (!(securityContext instanceof UserSecurityContext)) {
        return false;
      }

      UserSecurityContext userSecurityContext = (UserSecurityContext) securityContext;
      if (authorizationService.isAuthorized(resourceId, userSecurityContext.getUserRole())) {
        return true;
      }

      return false;
    }

    for (String role : defaultRoles) {
      if (securityContext.isUserInRole(role) || role.equals(Roles.ANY_USER)) {
        return true;
      }
    }

    return false;
  }

}
