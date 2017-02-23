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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

/**
 * Filter placed in front of all REST calls that handles the authentication of the users.
 *
 * @author mhoekstra
 */
@Provider
@PreMatching
@Priority(value = Priorities.AUTHENTICATION)
public class AuthenticationRequestFilter implements ContainerRequestFilter {

  private static final Logger AUTHENTICATION_LOGGER = LoggerFactory
      .getLogger("authenticationLogger");
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationRequestFilter.class
      .getName());
  /**
   * The path where the login call on the API is placed.
   * 
   * Hard loginPath to allow for anonymous access to the login url. This should be changed to a role
   * definition for allowing anonymous access on certain resources.
   */
  private final String loginPath = "client/login";

  @Autowired
  private List<AuthenticationManager> authenticationManagers;

  @Inject
  private javax.inject.Provider<org.glassfish.grizzly.http.server.Request> request;


  /**
   * Checks of Authentication Token for all URI's other than Login URI.
   *
   * @param requestContext describing the call
   */
  @Override
  public final void filter(final ContainerRequestContext requestContext) {
    String path = requestContext.getUriInfo().getPath();
    String ip = request != null ? request.get().getRemoteAddr() : "unknown";
    LOGGER.info(String.format("LCMRESTRequestFilter called with request path %s", path));
    if (requestContext.getRequest().getMethod().equals("OPTIONS")) {
      requestContext.abortWith(Response.status(Response.Status.OK).build());
      return;
    }
    if (!path.equals(loginPath)) {
      for (AuthenticationManager authenticationManager : authenticationManagers) {
        if (authenticationManager.isEnabled()
            && authenticationManager.isAuthenticationValid(requestContext)) {
          SecurityContext securityContext =
              authenticationManager.getSecurityContext(requestContext);

          if (securityContext == null) {
            continue;
          }

          AUTHENTICATION_LOGGER.info("AuthenticationRequestFilter authenticates with: "
              + authenticationManager.getClass().getName() + " user: "
              + securityContext.getUserPrincipal().getName() + " IP: " + ip);

          requestContext.setSecurityContext(securityContext);

          return;
        }
      }

      AUTHENTICATION_LOGGER.info("AuthenticationRequestFilter was not able to authenticate user. " + " IP: " + ip);
      requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
          .entity("You are not authorized to access LCM!").build());

    }
  }
}
