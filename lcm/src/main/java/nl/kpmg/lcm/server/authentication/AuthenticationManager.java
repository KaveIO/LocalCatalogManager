package nl.kpmg.lcm.server.authentication;

import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.SecurityContext;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.LogoutException;

import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Authentication Manager.
 *
 * @author venkateswarlub
 */
public class AuthenticationManager {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationManager.class);

    /**
     * The user service.
     */
    private final UserService userService;

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

    /**
     * Default constructor.
     *
     * @param userService the user service
     */
    @Autowired
    public AuthenticationManager(final UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public final void setAdminUser(final String adminUser) {
        this.adminUser = adminUser;
    }

    @Autowired
    public final void setAdminPassword(final String adminPassword) {
        this.adminPassword = adminPassword;
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
        if (username.equals(adminUser)) {
            LOGGER.info("Caught login attempt for admin user");
            if (password.equals(adminPassword)) {
                return createAuthenticationToken(adminUser, Roles.ADMINISTRATOR, UserOrigin.CONFIGURED);
            }
        } else {
            LOGGER.info("Caught login attempt for regular user");
            User user = userService.getUserDao().getUser(username);

            try {
                if (user != null && user.passwordEquals(password)) {
                    return createAuthenticationToken(username, user.getRole(), UserOrigin.LOCAL);
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                LOGGER.error("Something went wrong with the password hashing algorithm", ex);
                throw new LoginException("Authentication Failed", ex);
            }
        }
        throw new LoginException("Authentication Failed");
    }

    /**
     * Checks if given authentication token is valid for given user.
     *
     * @param username to check
     * @param authenticationToken the validate against
     * @return True if the token is valid
     */
    public final boolean isAuthenticationTokenValid(final String username, final String authenticationToken) {
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
     * Removes given authentication token from the authentication token map.
     *
     * @param authenticationToken the token to log out
     * @throws LogoutException if the token didn't exist
     */
    public final void logout(final String authenticationToken) throws LogoutException {
        LOGGER.info(String.format("Removing authentication token %s", authenticationToken));
        if (authenticationTokenMap.containsKey(authenticationToken)) {
            authenticationTokenMap.remove(authenticationToken);
            return;
        }
        throw new LogoutException("logout Failed");
    }

    /**
     * Creates a new token for given username and registers it in the the token
     * map.
     *
     * @param username the username to create token for
     * @return the registered authentication token.
     */
    private String createAuthenticationToken(final String username, String role, UserOrigin userOrigin) {
        LOGGER.info(String.format("Creating new authentication token for %s", username));
        String authenticationToken = UUID.randomUUID().toString();
        authenticationTokenMap.put(authenticationToken, new Session(username, role, userOrigin));
        return authenticationToken;
    }

    public UserSecurityContext getSecurityContext(String authenticationToken) {
        if (authenticationTokenMap.containsKey(authenticationToken)) {
            Session session = authenticationTokenMap.get(authenticationToken);
            return new UserSecurityContext(session);
        }
        return null;
    }
}
