package nl.kpmg.lcm.server.authentication;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.LCMBaseTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.LogoutException;
import nl.kpmg.lcm.server.rest.authentication.SessionAuthenticationManager;
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
public class SessionAuthenticationManagerTest extends LCMBaseTest {

    @Autowired
    private SessionAuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    /**
     * Used for opening the private method as public for specific tests.
     */
    Method isAuthenticationTokenValid;

    public SessionAuthenticationManagerTest() {
        try {
            isAuthenticationTokenValid = SessionAuthenticationManager.class.getDeclaredMethod(
                    "isAuthenticationTokenValid", String.class, String.class);
            isAuthenticationTokenValid.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(SessionAuthenticationManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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
    public void testAuthenticationTokenIsValid() throws Exception {
        String authenticationToken = authenticationManager.getAuthenticationToken("admin", "admin");
        assertTrue((boolean) isAuthenticationTokenValid.invoke(authenticationManager, "admin", authenticationToken));
    }

    @Test
    public void testEmptyAuthenticationTokenIsNotValid() throws Exception {
        assertFalse((boolean) isAuthenticationTokenValid.invoke(authenticationManager, "admin", ""));
    }

    @Test
    public void testLogoutInvalidatesAuthenticationToken() throws Exception {
        String authenticationToken = authenticationManager.getAuthenticationToken("admin", "admin");
        assertTrue((boolean) isAuthenticationTokenValid.invoke(authenticationManager, "admin", authenticationToken));

        authenticationManager.removeAuthenticationToken(authenticationToken);
        assertFalse((boolean) isAuthenticationTokenValid.invoke(authenticationManager, "admin", authenticationToken));
    }

    @Test(expected = LogoutException.class)
    public void testLogoutThrowsOnInvalidAuthenticationToken() throws LoginException, LogoutException {
        authenticationManager.removeAuthenticationToken("");
    }

    @Test
    public void testGetAuthenticationTokenUsesUserObjects() throws Exception {
        User user = new User();
        user.setId("testUser");
        user.setPassword("testPassword", false);
        userService.getUserDao().persist(user);

        String authenticationToken = authenticationManager.getAuthenticationToken("testUser", "testPassword");
        assertNotNull(authenticationToken);
    }

    @Test
    public void testGetAuthenticationTokenPrefersConfiguredUserOverUserObjects() throws Exception {
        User user = new User();
        user.setId("admin");
        user.setPassword("testPassword", false);
        userService.getUserDao().persist(user);

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
