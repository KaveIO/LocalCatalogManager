/*
  * Copyright 2017 KPMG N.V. (unless otherwise stated).
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
package nl.kpmg.lcm.server.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 *
 * @author shristov
 */
public class SecurityEngineTest {

  @Test
  public void testEncryptionDecription() throws EncryptionException {
    String securityKey = "0123456789abcdef";
    String initVector = "alabala123456^^&";
    SecurityEngine sEngine = new SecurityEngine(initVector);

    String originalText = "sample text";

    String encrypted = sEngine.encrypt(securityKey, originalText);

    String decrypted = sEngine.decrypt(securityKey, encrypted);

    assertFalse(encrypted.equals(decrypted));
    assertTrue(originalText.equals(decrypted));
  }

  @Test(expected = EncryptionException.class)
  public void testWrongDecription() throws EncryptionException {
    String securityKey = "0123456789abcdef";
    String initVector = "alabala123456^^&";
    SecurityEngine sEngine = new SecurityEngine(initVector);

    sEngine.decrypt(securityKey, "sample text");
  }
}