/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
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

package nl.kpmg.lcm.server.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import nl.kpmg.lcm.server.LcmBaseTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.LogoutException;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.authentication.SessionAuthenticationManager;

import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

/**
 * @author venkateswarlub
 *
 */
public class SessionAuthenticationManagerTest extends LcmBaseTest {

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
      isAuthenticationTokenValid = SessionAuthenticationManager.class
          .getDeclaredMethod("isAuthenticationTokenValid", String.class, String.class);
      isAuthenticationTokenValid.setAccessible(true);
    } catch (NoSuchMethodException | SecurityException ex) {
        LoggerFactory.getLogger(SessionAuthenticationManagerTest.class.getName()).error(null, ex);
    }
  }

  @Test
  public void testGetAuthenticationToken() throws LoginException {
    String authenticationToken = authenticationManager.getAuthenticationToken("admin", "admin");
    assertNotNull(authenticationToken);
  }

  @Test(expected = LoginException.class)
  public void testGetAuthenticationTokenThrowsOnBadPassword() throws LoginException {
    String authenticationToken =
        authenticationManager.getAuthenticationToken("admin", "not_admin_password");
  }

  @Test
  public void testAuthenticationTokenIsValid() throws Exception {
    String authenticationToken = authenticationManager.getAuthenticationToken("admin", "admin");
    assertTrue((boolean) isAuthenticationTokenValid.invoke(authenticationManager, "admin",
        authenticationToken));
  }

  @Test
  public void testEmptyAuthenticationTokenIsNotValid() throws Exception {
    assertFalse((boolean) isAuthenticationTokenValid.invoke(authenticationManager, "admin", ""));
  }

  @Test
  public void testLogoutInvalidatesAuthenticationToken() throws Exception {
    String authenticationToken = authenticationManager.getAuthenticationToken("admin", "admin");
    assertTrue((boolean) isAuthenticationTokenValid.invoke(authenticationManager, "admin",
        authenticationToken));

    authenticationManager.removeAuthenticationToken(authenticationToken);
    assertFalse((boolean) isAuthenticationTokenValid.invoke(authenticationManager, "admin",
        authenticationToken));
  }

  @Test(expected = LogoutException.class)
  public void testLogoutThrowsOnInvalidAuthenticationToken()
      throws LoginException, LogoutException {
    authenticationManager.removeAuthenticationToken("");
  }

  @Test
  public void testGetAuthenticationTokenUsesUserObjects() throws Exception {
    User user = new User();
    user.setName("testUser");
    user.setPassword("testPassword");
    userService.getUserDao().save(user);

    String authenticationToken =
        authenticationManager.getAuthenticationToken("testUser", "testPassword");
    assertNotNull(authenticationToken);
  }

  @Test
  public void testGetAuthenticationTokenPrefersConfiguredUserOverUserObjects() throws Exception {
    User user = new User();
    user.setName("admin");
    user.setPassword("testPassword");
    userService.getUserDao().save(user);

    String authenticationToken = authenticationManager.getAuthenticationToken("admin", "admin");
    assertNotNull(authenticationToken);

    try {
      authenticationManager.getAuthenticationToken("admin", "testPassword");
      fail();
    } catch (LoginException e) {
      assertTrue(true);
    }
  }
}
