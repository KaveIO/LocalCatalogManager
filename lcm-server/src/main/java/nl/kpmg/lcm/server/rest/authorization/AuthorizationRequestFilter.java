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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import javax.annotation.Priority;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author shristov
 */
@Provider
@Priority(value = Priorities.AUTHORIZATION)
public class AuthorizationRequestFilter implements ContainerRequestFilter {
  private static final Logger AUTHORIZATION_LOGGER = LoggerFactory.getLogger("authorizationLogger");
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationRequestFilter.class
      .getName());

  @Inject
  private ResourceInfo resourceInfo;

  @Autowired
  private PermissionChecker permissionChecker;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String path = requestContext.getUriInfo().getPath();
    if (path == null) {
      LOGGER.error("Path is null!");
      requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Error occured, please contact the system administrator!").build());
      return;
    }

    RolesAllowed annotation = resourceInfo.getResourceMethod().getAnnotation(RolesAllowed.class);
    if (annotation == null) {
      LOGGER
          .warn("Path: " + path + " does not have allowed roles and nobody can access it! User: \""
              + requestContext.getSecurityContext().getUserPrincipal().getName()
              + "\" has just tried.");

      requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
          .entity("You are not authorized to access: " + path).build());
      return;
    }

    SecurityContext securityContext = requestContext.getSecurityContext();
    if (securityContext == null) {
      AUTHORIZATION_LOGGER.error("Security context is null! Path: " + path);

      requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Error occured, please contact the system administrator!").build());
      return;
    }

    if (!permissionChecker.check(securityContext, path, annotation.value())) {
      AUTHORIZATION_LOGGER.warn("User: " + securityContext.getUserPrincipal().getName()
          + " tried to access " + path + " but it is not authorized!");
      requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
          .entity("You are not authorized to access: " + path).build());
      return;
    }

    String userName =
        securityContext.getUserPrincipal() != null ? "User "
            + securityContext.getUserPrincipal().getName() : "Unknown user";
    AUTHORIZATION_LOGGER.info(userName + " was successfully authorized to access: " + path);

  }
}
