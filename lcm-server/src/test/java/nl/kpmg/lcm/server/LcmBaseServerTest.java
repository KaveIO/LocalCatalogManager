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

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.client.HttpsClientFactory;
import nl.kpmg.lcm.common.configuration.ClientConfiguration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.ws.rs.client.WebTarget;

public abstract class LcmBaseServerTest extends LcmBaseTest {

  /**
   * Token used for login as admin. Contains: Basic BASE64(admin + ":" + admin).
   */
  protected static String basicAuthTokenAdmin = "Basic YWRtaW46YWRtaW4=";
  protected static String testAdminUsername = "admin";
  protected static String testAdminPassword = "admin";

  protected static Server server;

  protected static HttpsClientFactory httpsClientFactory;

  /**
   * Read configuration to return HTTP or HTTPS UR.
   */
  @Autowired
  @Value("${lcm.server.unsafe}")
  private String serverUnsafe;

  @Autowired
  @Value("${lcm.client.security.truststore}")
  private String serverTruststore;

  @Autowired
  @Value("${lcm.server.security.keystorePassword}")
  private String serverTruststorePass;

  @Autowired
  @Value("${lcm.server.security.keystoreType}")
  private String serverTruststoreType;

  @BeforeClass
  public static void setUpClass() throws Exception {
    try {
      LcmBaseTest.setUpClass();
      server = new Server();
      server.start();

    } catch (ServerException se) {
      LoggerFactory.getLogger(LcmBaseServerTest.class.getName()).error(
              "Failed to create HTTPS server or client, test will fail", se);
    }
  }

  @AfterClass
  public static void tearDownClass() {
    LcmBaseTest.tearDownClass();
    server.stop();
  }

  protected WebTarget getWebTarget() throws ServerException {
    if(httpsClientFactory == null){


      ClientConfiguration clientConfiguration = new ClientConfiguration();
      clientConfiguration.setTruststore(serverTruststore);
      clientConfiguration.setTruststorePassword(serverTruststorePass);
      clientConfiguration.setTruststoreType(serverTruststoreType);
      clientConfiguration.setTargetHost("0.0.0.0");

      httpsClientFactory = new HttpsClientFactory(clientConfiguration);

    }

    if (Boolean.valueOf(serverUnsafe)) {
      return httpsClientFactory.createWebTarget(server.getBaseFallbackUri());
    } else {
      return httpsClientFactory.createWebTarget(server.getBaseUri());
    }

  }
}
