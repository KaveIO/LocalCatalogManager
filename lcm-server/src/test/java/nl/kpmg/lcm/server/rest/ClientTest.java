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

package nl.kpmg.lcm.server.rest;

import static org.junit.Assert.assertEquals;

import nl.kpmg.lcm.server.LcmBaseServerTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.rest.client.types.LoginRequest;

import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class ClientTest extends LcmBaseServerTest {

  /**
   * Test to see that the client interface returns the interface versions.
   *
   * @throws ServerException
   */
  @Test
  public void testGetClientInterfaceVersions() throws LoginException, ServerException {
    // Due to a Spring configuration error we can't login in this thread. We
    // have to create a actuall login call.
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setUsername("admin");
    loginRequest.setPassword("admin");
    Entity<LoginRequest> entity = Entity.entity(loginRequest,
        "application/nl.kpmg.lcm.server.rest.client.types.LoginRequest+json");
    Response res = getWebTarget().path("client/login").request().post(entity);

    Response result =
        getWebTarget().path("client").request().header("LCM-Authentication-User", "admin")
            .header("LCM-Authentication-Token", res.readEntity(String.class)).get();
    assertEquals(200, result.getStatus());
  }
}
