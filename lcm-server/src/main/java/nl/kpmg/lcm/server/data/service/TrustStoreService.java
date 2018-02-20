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
package nl.kpmg.lcm.server.data.service;

import nl.kpmg.lcm.common.exception.LcmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author shristov
 */
@Service
public class TrustStoreService {
  private static final Logger logger = LoggerFactory.getLogger(TrustStoreService.class.getName());

  private String trustStoreFilename;

  private String trustStorePassword;

  @Value("${lcm.client.security.truststore}")
  public final void setTrustStoreFilename(final String filename) {
    this.trustStoreFilename = filename;
  }

  @Value("${lcm.client.security.truststorePassword}")
  public final void setTrustStorePassword(final String password) {
    this.trustStorePassword = password;
  }

  public void addCertificate(byte[] certificateAsBytes, String alias) {
    try {
      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

      char[] password = trustStorePassword.toCharArray();
      try (FileInputStream fis = new FileInputStream(trustStoreFilename)) {
        ks.load(fis, password);
      }

      ByteArrayInputStream bis = new ByteArrayInputStream(certificateAsBytes);
            X509Certificate ca =
          (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(
              new BufferedInputStream(bis));

      ks.setCertificateEntry(alias, ca);

      try (FileOutputStream fos = new FileOutputStream(trustStoreFilename)) {
        ks.store(fos, password);
      }

    } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
        new LcmException("Unable to add Certificate. Alias: " + alias, e);
    }
  }

  public void removeCertificate(String alias) {
    try {
      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

      char[] password = trustStorePassword.toCharArray();
      try (FileInputStream fis = new FileInputStream(trustStoreFilename)) {
        ks.load(fis, password);
      }

      ks.deleteEntry(alias);

      try (FileOutputStream fos = new FileOutputStream(trustStoreFilename)) {
        ks.store(fos, password);
      }

    } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
        new LcmException("Unable to remove Certificate. Alias: " + alias, e);
    }
  }

  public List<String> listTrustStoreAliases() {

    List<String> result = new LinkedList();

    try {
      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

      // get user password and file input stream
      char[] password = trustStorePassword.toCharArray();
      try (FileInputStream fis = new FileInputStream(trustStoreFilename)) {
        ks.load(fis, password);
      }

      Enumeration<String> aliases = ks.aliases();
      while (aliases.hasMoreElements()) {
        String alias = aliases.nextElement();
        result.add(alias);
      }
      return result;
    } catch (Exception e) {
      logger.info("Unable to load truststore aliases. Message: " + e.getMessage());
      return null;
    }
  }

}