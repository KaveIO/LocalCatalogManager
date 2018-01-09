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

package nl.kpmg.lcm.server.rest.client.version0;

import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.BASIC_AUTHENTICATION_HEADER;
import static org.junit.Assert.assertEquals;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.server.LcmBaseServerTest;
import nl.kpmg.lcm.server.LoginException;

import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class LocalControllerClientTest extends LcmBaseServerTest {

  @Test
  public void testGetLocalOverview() throws LoginException, ServerException {
    Response response = getWebTarget().path("client/v0/local").request()
        .header(BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).get();

    assertEquals(Status.OK.getStatusCode(), response.getStatus());
  }
}
