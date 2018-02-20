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
package nl.kpmg.lcm.server.rest.remote.version0;

import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.rest.authentication.Roles;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author shristov
 */
@Path("remote/v0/test")
public class RemoteLcmTestConectivityController {

  @Autowired
  private MetaDataService metaDataService;

  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.MetaDataRepresentation+json"})
  @RolesAllowed({Roles.ANY_USER})
  public final String testConnectivity() {

    return "OK";
  }
}