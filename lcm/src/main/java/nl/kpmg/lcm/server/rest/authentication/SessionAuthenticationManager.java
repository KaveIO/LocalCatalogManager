package nl.kpmg.lcm.server.rest.authentication;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.container.ContainerRequestContext;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.LogoutException;

import nl.kpmg.lcm.server.data.service.UserService;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Authentication Manager.
 *
 * @author venkateswarlub
 */
public class SessionAuthenticationManager extends AuthenticationManager {
    /**
     * The name of the http request header containing the authentication user.
     */
    public static final String LCM_AUTHENTICATION_USER_HEADER = "LCM-Authentication-User";

    /**
     * The name of the http request header containing the authentication token.
     */
    public static final String LCM_AUTHENTICATION_TOKEN_HEADER = "LCM-Authentication-Token";

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionAuthenticationManager.class);

    /**
     * The map containing all users that have been properly authenticated.
     */
    private final Map<String, Session> authenticationTokenMap = new HashMap();

    @Autowired
    public SessionAuthenticationManager(UserService userService) {
        super(userService);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public final boolean isAuthenticationValid(final ContainerRequestContext requestContext) {
        String username = requestContext.getHeaderString(LCM_AUTHENTICATION_USER_HEADER);
        String authenticationToken = requestContext.getHeaderString(LCM_AUTHENTICATION_TOKEN_HEADER);

        return isAuthenticationTokenValid(username, authenticationToken);
    }

    @Override
    public final UserSecurityContext getSecurityContext(final ContainerRequestContext requestContext) {
        String authenticationToken = requestContext.getHeaderString(LCM_AUTHENTICATION_TOKEN_HEADER);

        if (authenticationTokenMap.containsKey(authenticationToken)) {
            Session session = authenticationTokenMap.get(authenticationToken);
            return new UserSecurityContext(session);
        }
        return null;
    }

    /**
     * Validate username and password, and return authentication token if
     * successful.
     *
     * @param username of the logging in user
     * @param password of the logging in user
     * @return the authentication token
     * @throws LoginException if the authentication fails
     */
    public final String getAuthenticationToken(final String username, final String password)
            throws LoginException {

        if (isUsernamePasswordValid(username, password)) {
            Session session = createSessionForUser(username);
            return createAuthenticationToken(session);
        } else {
            throw new LoginException("Authentication Failed");
        }
    }

    /**
     * Removes given authentication token from the authentication token map.
     *
     * @param authenticationToken the token to log out
     * @throws LogoutException if the token didn't exist
     */
    public final void removeAuthenticationToken(final String authenticationToken) throws LogoutException {
        LOGGER.info(String.format("Removing authentication token %s", authenticationToken));
        if (authenticationTokenMap.containsKey(authenticationToken)) {
            authenticationTokenMap.remove(authenticationToken);
            return;
        }
        throw new LogoutException("logout Failed");
    }

    /**
     * Checks if given authentication token is valid for given user.
     *
     * @param username to check
     * @param authenticationToken the validate against
     * @return True if the token is valid
     */
    private boolean isAuthenticationTokenValid(final String username, final String authenticationToken) {
        if (authenticationTokenMap.containsKey(authenticationToken)) {
            Session session = authenticationTokenMap.get(authenticationToken);
            if (username.equals(session.getUsername())) {
                session.updateLastSeen();
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new token for given username and registers it in the the token
     * map.
     *
     * @param session the session to create token for
     * @return the registered authentication token.
     */
    private String createAuthenticationToken(final Session session) {
        LOGGER.info(String.format("Creating new authentication token for %s", session.getUsername()));
        String authenticationToken = UUID.randomUUID().toString();
        authenticationTokenMap.put(authenticationToken, session);
        return authenticationToken;
    }
}
