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

package nl.kpmg.lcm.server.rest.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.kpmg.lcm.server.LcmBaseServerTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.rest.authentication.BasicAuthenticationManager;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

public class Version0ClientTest extends LcmBaseServerTest {

  @Test
  public void testGetIndex() throws LoginException, IOException, ServerException {
    Response response = getWebTarget().path("client/v0").request()
        .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).get();

    assertEquals(200, response.getStatus());

    String result = response.readEntity(String.class);
    ObjectMapper objectMapper = new ObjectMapper();
    Map responseMap = objectMapper.readValue(result, Map.class);

    assertTrue(responseMap.containsKey("links"));
    List responseLinkList = (List) responseMap.get("links");
    assertEquals(7, responseLinkList.size());
  }
}
