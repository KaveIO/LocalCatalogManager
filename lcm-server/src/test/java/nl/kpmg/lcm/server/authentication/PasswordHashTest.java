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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import nl.kpmg.lcm.server.rest.authentication.PasswordHash;
import nl.kpmg.lcm.server.rest.authentication.UserPasswordHashException;

import org.junit.Test;

/**
 * @author venkateswarlub
 *
 */
public class PasswordHashTest {

  @Test
  public void testHashesDifferInIterations() throws UserPasswordHashException {
    for (int i = 0; i < 100; i++) {
      String password = "" + i;
      String hash = PasswordHash.createHash(password);
      String secondHash = PasswordHash.createHash(password);
      assertNotSame(hash, secondHash);
    }
  }

  @Test
  public void testWrongPasswordDoesNotValidate() throws UserPasswordHashException {
    for (int i = 0; i < 100; i++) {
      String password = "" + i;
      String wrongPassword = "" + i + 1;
      String hash = PasswordHash.createHash(password);

      assertFalse(PasswordHash.validatePassword(wrongPassword, hash));
    }
  }

  @Test
  public void testGoodPasswordDoesValidate() throws UserPasswordHashException {
    for (int i = 0; i < 100; i++) {
      String password = "" + i;
      String hash = PasswordHash.createHash(password);
      assertTrue(PasswordHash.validatePassword(password, hash));
    }
  }
}
