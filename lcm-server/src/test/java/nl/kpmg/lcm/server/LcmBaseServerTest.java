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

package nl.kpmg.lcm.server;

import nl.kpmg.lcm.client.HttpsClientFactory;
import nl.kpmg.lcm.configuration.ClientConfiguration;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.WebTarget;

public abstract class LcmBaseServerTest extends LcmBaseTest {

  /**
   * Token used for login as admin. Contains: Basic BASE64(admin + ":" + admin).
   */
  protected static String basicAuthTokenAdmin = "Basic YWRtaW46YWRtaW4=";

  protected static Server server;

  protected static HttpsClientFactory httpsClientFactory;

  @BeforeClass
  public static void setUpClass() throws Exception {
    try {
      LcmBaseTest.setUpClass();
      server = new Server();
      server.start();

      ClientConfiguration clientConfiguration = new ClientConfiguration();
      clientConfiguration.setKeystore("./src/test/resources/client-keystore.jks");
      clientConfiguration.setKeystoreType("JKS");
      clientConfiguration.setKeystorePassword("storepass");
      clientConfiguration.setKeystoreAlias("cert-ui");
      clientConfiguration.setKeystoreKeypass("keypass");
      clientConfiguration.setTruststore("./src/test/resources/client-truststore.jks");
      clientConfiguration.setTruststorePassword("storepass");
      clientConfiguration.setTruststoreType("JKS");

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
    return httpsClientFactory.createWebTarget(server.getBaseUri());
  }
}
