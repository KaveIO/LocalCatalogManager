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

import nl.kpmg.lcm.common.Roles;
import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.data.UserGroup;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.data.service.UserGroupService;
import nl.kpmg.lcm.server.data.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author shristov
 */
@Service
public class PermissionChecker {
  private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("auditLogger");

  private AuthorizationService authorizationService;

  @Autowired
  private UserGroupService userGroupService;

  @Autowired
  private UserService userService;

  @Autowired
  private MetaDataService metadataService;

  public PermissionChecker() {}

  public PermissionChecker(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  static private ThreadLocal threadLocal = new ThreadLocal<User>();

  public static ThreadLocal<User> getThreadLocal() {
    return threadLocal;
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
      User principal = (User) securityContext.getUserPrincipal();
      if (authorizationService.isAuthorized(resourceId, principal.getRole())) {
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

  public boolean check(SecurityContext securityContext, String metadataId) {
    if (securityContext == null || metadataId == null) {
      AUDIT_LOGGER.info("Unable to authorize access to metadata: " + metadataId
          + " because security context is null.");
      return false;
    }

    User principal = (User) securityContext.getUserPrincipal();

    // TODO administratore role has full acess?
    if (principal.getRole().equals(Roles.ADMINISTRATOR)) {
      AUDIT_LOGGER.info("Authorized admin user: " + principal.getName()
          + " to access metadata: " + metadataId + ".");
      return true;
    }

    User user = userService.findOneByNameAndOrigin(principal.getName(), principal.getOrigin());

    if (user == null) {
      AUDIT_LOGGER.info("Unable to authorize access to metadata:" + metadataId
          + " because user is not found: " + principal.getName() + "@" + principal.getOrigin() + ".");
      return false;
    }

    MetaData metadata = metadataService.findById(metadataId);
    if (metadata == null) {
      AUDIT_LOGGER.info("Unable to authorize access to metadata: " + metadataId
          + " because metadata is not found. User:" + user.getId() + ".");
      return false;
    }

    MetaDataWrapper metadataWrapper = new MetaDataWrapper(metadata);

    if (isUserDirectlyAuthorized(user, metadataWrapper)) {
      return true;
    }

    if (isUserAuthorizedByUserGroup(user, metadataWrapper)) {
      return true;
    }

    AUDIT_LOGGER.info("Unable to authorize access to metadata: " + metadataId
        + " because no permissions are not found for user:" + user.getId() + ".");
    return false;
  }

  private boolean isUserAuthorizedByUserGroup(User user, MetaDataWrapper metadataWrapper) {
    List<UserGroup> userGroupList = userGroupService.findByUserId(user.getId());
    for (UserGroup group : userGroupList) {
      if (group.getAllowedMetadataList() != null) {
        for (String id : group.getAllowedMetadataList()) {
          if (metadataWrapper.getId().equals(id)) {
            AUDIT_LOGGER.info("Authorized user: " + user.getId() + " to access metadata: "
                + metadataWrapper.getId()
                + ". Reason : User is part of directly authorized group: " + group.getId() + ".");
            return true;
          }
        }
      }
    }
    for (UserGroup group : userGroupList) {
      if (group.getAllowedPathList() != null) {
        for (String path : group.getAllowedPathList()) {
          if (metadataWrapper.getData().getPath().startsWith(path)) {
            AUDIT_LOGGER.info("Authorized user: " + user.getId() + " to access metadata: "
                + metadataWrapper.getId() + ". Reason : User is part of authorized group:"
                + group.getId() + ".");
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean isUserDirectlyAuthorized(User user, MetaDataWrapper metadataWrapper) {
    if (user.getAllowedMetadataList() != null) {
      for (String id : user.getAllowedMetadataList()) {
        if (metadataWrapper.getId().equals(id)) {
          AUDIT_LOGGER.info("Authorized user: " + user.getId() + " to access metadata: "
              + metadataWrapper.getId() + ". Reason : User is directly authorized.");
          return true;
        }
      }
    }
    if (user.getAllowedPathList() != null) {
      for (String path : user.getAllowedPathList()) {
        if (metadataWrapper.getData().getPath().startsWith(path)) {
          AUDIT_LOGGER.info("Authorized user: " + user.getId() + " to access metadata: "
              + metadataWrapper.getId() + ". Reason : User is authorized by path.");
          return true;
        }
      }
    }
    return false;
  }
}