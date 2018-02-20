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

import static org.junit.Assert.*;

import nl.kpmg.lcm.server.LcmBaseServerTest;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
/**
 *
 * @author shristov
 */
public class TrustStoreServiceTest extends LcmBaseServerTest {

  
  private String certificate;
  private String certificateAlias1 =  "lcm-server1";

  @Value("${lcm.server.security.test.certificate1}")
  public final void setCertificate(final String certificate) {
    this.certificate = certificate;
  }

  private String certificate2;
  private String certificateAlias2 =  "lcm-server2";

  @Value("${lcm.server.security.test.certificate1}")
  public final void setCertificate2(final String certificate) {
    this.certificate2 = certificate;
  }


  @Autowired
  private TrustStoreService service;


  private int originalCertificatesCount = 0;

  @Before
  public void setUp() {
    List<String> aliases = service.listTrustStoreAliases();
    originalCertificatesCount = aliases.size();
    for (String alias : aliases) {
      if (alias.equals(certificateAlias2) || alias.equals(certificateAlias1)) {
        service.removeCertificate(alias);
        originalCertificatesCount--;
      }
    }
  }

  @Test
  public void testAddCertificate() throws IOException {
    byte[] certificateAsBytes = IOUtils.toByteArray(new FileInputStream(certificate));

    service.addCertificate(certificateAsBytes, certificateAlias1);
    List<String> aliases = service.listTrustStoreAliases();
    assertEquals(originalCertificatesCount + 1, aliases.size());

    certificateAsBytes = IOUtils.toByteArray(new FileInputStream(certificate2));
    service.addCertificate(certificateAsBytes, certificateAlias2);
    aliases = service.listTrustStoreAliases();
    assertEquals(originalCertificatesCount + 2, aliases.size());
  }

  @Test
  public void testRemoveCertificate() throws IOException {
    byte[] certificateAsBytes = IOUtils.toByteArray(new FileInputStream(certificate));
    service.addCertificate(certificateAsBytes, certificateAlias1);

    certificateAsBytes = IOUtils.toByteArray(new FileInputStream(certificate2));
    service.addCertificate(certificateAsBytes, certificateAlias2);

    List<String> aliases;
    aliases = service.listTrustStoreAliases();
    assertEquals(originalCertificatesCount + 2, aliases.size());

    service.removeCertificate(certificateAlias1);
    aliases = service.listTrustStoreAliases();
    assertEquals(originalCertificatesCount + 1, aliases.size());

    service.removeCertificate(certificateAlias2);
    aliases = service.listTrustStoreAliases();
    assertEquals(originalCertificatesCount, aliases.size());
  }

  @Test
  public void testListCertificate() throws IOException {
    byte[] certificateAsBytes = IOUtils.toByteArray(new FileInputStream(certificate));

    service.addCertificate(certificateAsBytes, certificateAlias1);
    List<String> aliases = service.listTrustStoreAliases();
    assertEquals(originalCertificatesCount + 1, aliases.size());
    assertEquals(aliases.get(0), certificateAlias1);
  }

}
