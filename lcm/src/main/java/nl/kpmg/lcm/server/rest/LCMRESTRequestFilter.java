package nl.kpmg.lcm.server.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import nl.kpmg.lcm.server.authentication.AuthenticationManager;
import nl.kpmg.lcm.server.authentication.UserSecurityContext;
import nl.kpmg.lcm.server.data.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Filter placed in front of all REST calls that handles the authentication of the users.
 *
 * @author mhoekstra
 */
@Provider
@PreMatching
public class LCMRESTRequestFilter implements ContainerRequestFilter {

    /**
     * The name of the http request header containing the authentication user.
     */
    public static final String LCM_AUTHENTICATION_USER_HEADER = "LCM-Authentication-User";

    /**
     * The name of the http request header containing the authentication token.
     */
    public static final String LCM_AUTHENTICATION_TOKEN_HEADER = "LCM-Authentication-Token";

    /**
     * The class logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LCMRESTRequestFilter.class.getName());

    /**
     * The authentication manager.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * The user service.
     */
    private final UserService userService;

    /**
     * The path where the login call on the API is placed.
     */
    private final String loginPath;

    /**
     * Default constructor.
     *
     * @param authenticationManager The central manager of authentications
     * @param userService The service used to find the users for the SecurityContext
     */
    @Autowired
    public LCMRESTRequestFilter(final AuthenticationManager authenticationManager, final UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;

        /*
         * Hard loginPath to allow for anonymous access to the login url. This
         * should be changed to a role defintion for allowing anonymous access
         * on certain resources.
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
            String username = requestContext.getHeaderString(LCM_AUTHENTICATION_USER_HEADER);
            String authenticationToken = requestContext.getHeaderString(LCM_AUTHENTICATION_TOKEN_HEADER);

            LOGGER.log(Level.INFO, "LCMRESTRequestFilter called with request authorizationToken {0}",
                    authenticationToken);
            if (authenticationManager.isAuthenticationTokenValid(username, authenticationToken)) {
                UserSecurityContext userSecurityContext = authenticationManager.getSecurityContext(authenticationToken);
                requestContext.setSecurityContext(userSecurityContext);
            } else {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .entity("You are not Authorized to access LCM").build());
            }
        }
    }
}
