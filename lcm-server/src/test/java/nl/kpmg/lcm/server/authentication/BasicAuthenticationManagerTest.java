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

package nl.kpmg.lcm.server.authentication;


import static org.junit.Assert.assertEquals;

import nl.kpmg.lcm.server.LcmBaseServerTest;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.authentication.BasicAuthenticationManager;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.Response;

/**
 * @author venkateswarlub
 *
 */
public class BasicAuthenticationManagerTest extends LcmBaseServerTest {

  @Autowired
  private BasicAuthenticationManager authenticationManager;

  @Autowired
  private UserService userService;

  @Test
  public void testGetUserTarget() throws LoginException, ServerException {
    Response res1 = getWebTarget().path("client/v0/users").request()
        .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).get();

    assertEquals(200, res1.getStatus());
  }

  @Test
  public void testGetLocalTarget() throws LoginException, ServerException {
    Response res1 = getWebTarget().path("client/v0/local").request()
        .header(BasicAuthenticationManager.BASIC_AUTHENTICATION_HEADER, basicAuthTokenAdmin).get();

    assertEquals(200, res1.getStatus());
  }
}
