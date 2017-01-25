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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.server.rest.authentication.Session;
import nl.kpmg.lcm.server.rest.authentication.UserOrigin;
import nl.kpmg.lcm.server.rest.authentication.UserSecurityContext;

import org.junit.Test;

/**
 *
 * @author shristov
 */
public class PermissionCheckerTest {

  private static PermissionChecker simpleChecker = new PermissionChecker();
  private static ExternalAuthorizationServiceMock authService =
      new ExternalAuthorizationServiceMock();
  private static PermissionChecker externalChecker = new PermissionChecker(authService);

  @Test
  public void testSimpleCheck() {
    Session session = new Session("admin", Roles.ADMINISTRATOR, UserOrigin.LOCAL, null);
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result =
        simpleChecker.check(securityContext, new String[] {Roles.ADMINISTRATOR, Roles.API_USER});
    assertTrue(result);
  }

  @Test
  public void testSimpleCheckUnauthorized() {
    Session session = new Session("admin", Roles.ADMINISTRATOR, UserOrigin.LOCAL, null);
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result =
        simpleChecker.check(securityContext, new String[] {Roles.REMOTE_USER, Roles.API_USER});
    assertFalse(result);
  }

  @Test
  public void testExternalCheck() {
    Session session = new Session("admin", Roles.ADMINISTRATOR, UserOrigin.LOCAL, null);
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result =
        externalChecker.check(securityContext, new String[] {Roles.ADMINISTRATOR, Roles.API_USER});
    assertTrue(result);
  }

  @Test
  public void testExternalCheckUnauthorized() {
    Session session = new Session("admin", Roles.ADMINISTRATOR, UserOrigin.LOCAL, null);
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result =
        externalChecker.check(securityContext, new String[] {Roles.ADMINISTRATOR, Roles.API_USER});
    assertFalse(result);
  }

  @Test
  public void testExternalCheckUnauthorizedMissingResourceId() {
    Session session = new Session("admin", Roles.ADMINISTRATOR, UserOrigin.LOCAL, null);
    UserSecurityContext securityContext = new UserSecurityContext(session);
    boolean result =
        externalChecker.check(securityContext, new String[] {Roles.REMOTE_USER, Roles.API_USER});
    assertFalse(result);
  }

  @Test
  public void testExternalCheckResourceId() {
    Session session = new Session("admin", Roles.ADMINISTRATOR, UserOrigin.LOCAL, null);
    UserSecurityContext securityContext = new UserSecurityContext(session);

    String resourceName = PermissionCheckerTest.class.getName() + ".testExternalCheck";
    boolean result =
        externalChecker.check(securityContext, resourceName, new String[] {Roles.ADMINISTRATOR,
            Roles.API_USER});
    assertTrue(result);
  }

  @Test
  public void testExternalCheckWithResourceIdUnauthorized() {
    Session session = new Session("admin", Roles.ADMINISTRATOR, UserOrigin.LOCAL, null);
    UserSecurityContext securityContext = new UserSecurityContext(session);

    String resourceName = PermissionCheckerTest.class.getName() + ".testExternalCheckUnauthorized";
    boolean result =
        externalChecker.check(securityContext, resourceName, new String[] {Roles.ADMINISTRATOR,
            Roles.API_USER});
    assertFalse(result);
  }

}
