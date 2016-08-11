package nl.kpmg.lcm.server.rest.authentication;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import nl.kpmg.lcm.server.data.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Filter placed in front of all REST calls that handles the authentication of the users.
 *
 * @author mhoekstra
 */
@Provider
@PreMatching
public class RequestFilter implements ContainerRequestFilter {

  /**
   * The class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(RequestFilter.class.getName());

  /**
   * The authentication manager.
   */
  private final BasicAuthenticationManager basicAuthenticationManager;

  /**
   * The authentication manager.
   */
  private final SessionAuthenticationManager sessionAuthenticationManager;

  /**
   * The path where the login call on the API is placed.
   */
  private final String loginPath;

  /**
   * Default constructor.
   *
   * @param sessionAuthenticationManager The manager for session based authentications
   * @param basicAuthenticationManager The manager for basic authentications
   * @param userService The service used to find the users for the SecurityContext
   */
  @Autowired
  public RequestFilter(final SessionAuthenticationManager sessionAuthenticationManager,
      final BasicAuthenticationManager basicAuthenticationManager, final UserService userService) {
    this.sessionAuthenticationManager = sessionAuthenticationManager;
    this.basicAuthenticationManager = basicAuthenticationManager;

    /*
     * Hard loginPath to allow for anonymous access to the login url. This should be changed to a
     * role defintion for allowing anonymous access on certain resources.
     */
    loginPath = "client/login";
  }

  /**
   * Checks of Authentication Token for all URI's other than Login URI.
   *
   * @param requestContext describing the call
   */
  @Override
  public final void filter(final ContainerRequestContext requestContext) {
    String path = requestContext.getUriInfo().getPath();

    LOGGER.log(Level.INFO, "LCMRESTRequestFilter called with request path {0}", path);
    if (requestContext.getRequest().getMethod().equals("OPTIONS")) {
      requestContext.abortWith(Response.status(Response.Status.OK).build());
      return;
    }
    if (!path.equals(loginPath)) {
      if (basicAuthenticationManager.isEnabled()
          && basicAuthenticationManager.isAuthenticationValid(requestContext)) {
        LOGGER.log(Level.INFO, "RequestFilter authenticates with basicAuthenticationManager");
        requestContext
            .setSecurityContext(basicAuthenticationManager.getSecurityContext(requestContext));
      } else if (sessionAuthenticationManager.isEnabled()
          && sessionAuthenticationManager.isAuthenticationValid(requestContext)) {
        LOGGER.log(Level.INFO, "RequestFilter authenticates with sessionAuthenticationManager");
        requestContext
            .setSecurityContext(sessionAuthenticationManager.getSecurityContext(requestContext));
      } else {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
            .entity("You are not Authorized to access LCM").build());
      }
    }
  }
}
