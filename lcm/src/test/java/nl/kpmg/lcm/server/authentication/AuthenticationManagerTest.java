package nl.kpmg.lcm.server.authentication;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import nl.kpmg.lcm.server.LCMBaseTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.LogoutException;
import nl.kpmg.lcm.server.authentication.AuthenticationManager;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.service.UserService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author venkateswarlub
 *
 */
public class AuthenticationManagerTest extends LCMBaseTest {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Test
    public void testGetAuthenticationToken() throws LoginException {
        String authenticationToken = authenticationManager.getAuthenticationToken("admin", "admin");
        assertNotNull(authenticationToken);
    }

    @Test(expected = LoginException.class)
    public void testGetAuthenticationTokenThrowsOnBadPassword() throws LoginException {
        String authenticationToken = authenticationManager.getAuthenticationToken("admin", "not_admin_password");
    }

    @Test
    public void testAuthenticationTokenIsValid() throws LoginException {
        String authenticationToken = authenticationManager.getAuthenticationToken("admin", "admin");
        assertTrue(authenticationManager.isAuthenticationTokenValid("admin", authenticationToken));
    }

    @Test
    public void testEmptyAuthenticationTokenIsNotValid() {
        assertFalse(authenticationManager.isAuthenticationTokenValid("admin", ""));
    }

    @Test
    public void testLogoutInvalidatesAuthenticationToken() throws LoginException, LogoutException {
        String authenticationToken = authenticationManager.getAuthenticationToken("admin", "admin");
        assertTrue(authenticationManager.isAuthenticationTokenValid("admin", authenticationToken));

        authenticationManager.logout(authenticationToken);
        assertFalse(authenticationManager.isAuthenticationTokenValid("admin", authenticationToken));
    }

    @Test(expected = LogoutException.class)
    public void testLogoutThrowsOnInvalidAuthenticationToken() throws LoginException, LogoutException {
        authenticationManager.logout("");
    }

    @Test
    public void testGetAuthenticationTokenUsesUserObjects() throws LoginException, LogoutException, NoSuchAlgorithmException, InvalidKeySpecException {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("testPassword", false);
        userService.getUserDao().saveUser(user);

        String authenticationToken = authenticationManager.getAuthenticationToken("testUser", "testPassword");
        assertNotNull(authenticationToken);
    }

    @Test
    public void testGetAuthenticationTokenPrefersConfiguredUserOverUserObjects() throws LoginException, LogoutException, NoSuchAlgorithmException, InvalidKeySpecException {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("testPassword", false);
        userService.getUserDao().saveUser(user);

        String authenticationToken = authenticationManager.getAuthenticationToken("admin", "admin");
        assertNotNull(authenticationToken);

        try {
            authenticationManager.getAuthenticationToken("admin", "testPassword");
            fail();
        }
        catch (LoginException e) {
            assertTrue(true);
        }
    }

}
