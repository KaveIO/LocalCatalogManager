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

import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author shristov
 */
public class SecurityEngine {
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SecurityEngine.class
      .getName());
  private IvParameterSpec iv;

  public SecurityEngine(String initVector) {
    try {
      iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException ex) {
      // Inietially do not log details to avoid storing sensitive data in the logs
      LOGGER.error("Unsupported encoding for init vector!");
    }
  }

  public String encrypt(String key, String value) throws EncryptionException {
    try {
      SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

      byte[] encrypted = cipher.doFinal(value.getBytes());

      return Base64.getEncoder().encodeToString(encrypted);
    } catch (Exception ex) {
      // Intentionally do not add details to avoid storing sensitive data in the logs
      throw new EncryptionException("Unable to encrypt data!", ex);
    }
  }

  public String decrypt(String key, String encrypted) throws EncryptionException {
    try {
      SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

      byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));

      return new String(original);
    } catch (Exception ex) {
      // Intentionally do not add details to avoid storing sensitive data in the logs
      throw new EncryptionException("Unable to decrypt data!", ex);
    }
  }
}