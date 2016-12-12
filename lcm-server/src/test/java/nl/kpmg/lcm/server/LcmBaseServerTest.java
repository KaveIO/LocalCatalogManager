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

package nl.kpmg.lcm.server;

import javax.ws.rs.client.WebTarget;

import nl.kpmg.lcm.client.HttpsClientFactory;
import nl.kpmg.lcm.configuration.ClientConfiguration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class LcmBaseServerTest extends LcmBaseTest {

  /**
   * Token used for login as admin. Contains: Basic BASE64(admin + ":" + admin).
   */
  protected static String basicAuthTokenAdmin = "Basic YWRtaW46YWRtaW4=";

  protected static Server server;

  protected static HttpsClientFactory httpsClientFactory;

  /**
   * Read configuration to return HTTP or HTTPS UR.
   */
  @Autowired
  @Value("${lcm.server.unsafe}")
  private String serverUnsafe;

  @BeforeClass
  public static void setUpClass() throws Exception {
    try {
      LcmBaseTest.setUpClass();
      server = new Server();
      server.start();

      ClientConfiguration clientConfiguration = new ClientConfiguration();
      clientConfiguration.setKeystore("./src/main/resources/ssl-keys/client.p12");
      clientConfiguration.setKeystoreType("pkcs12");
      clientConfiguration.setKeystorePassword("storepass");
      clientConfiguration.setKeystoreAlias("cert-ui");
      clientConfiguration.setKeystoreKeypass("storepass");
      clientConfiguration.setTruststore("./src/main/resources/ssl-keys/ui.keystore");
      clientConfiguration.setTruststorePassword("storepass");
      clientConfiguration.setTruststoreType("JKS");
      clientConfiguration.setTargetHost("0.0.0.0");

      httpsClientFactory = new HttpsClientFactory(clientConfiguration);

    } catch (ServerException se) {
      Logger.getLogger(LcmBaseServerTest.class.getName()).log(Level.SEVERE,
              "Failed to create HTTPS server or client, test will fail", se);
    }
  }

  @AfterClass
  public static void tearDownClass() {
    LcmBaseTest.tearDownClass();
    server.stop();
  }

  protected WebTarget getWebTarget() throws ServerException {
    if (Boolean.valueOf(serverUnsafe)) {
      return httpsClientFactory.createWebTarget(server.getBaseFallbackUri());
    } else {
      return httpsClientFactory.createWebTarget(server.getBaseUri());
    }

  }
}
