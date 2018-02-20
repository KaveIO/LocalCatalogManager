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
package nl.kpmg.lcm.server.rest.client.version0;

import nl.kpmg.lcm.common.data.LcmId;
import nl.kpmg.lcm.common.rest.types.LcmIdRepresentation;
import nl.kpmg.lcm.server.data.service.LcmIdService;
import nl.kpmg.lcm.common.Roles;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteLcmIdRepresentation;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * @author shristov
 */
@Path("client/v0/lcmId")
public class LcmIdController {
  @Autowired
  private LcmIdService lcmIdService;

  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.LcmIdRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public final LcmIdRepresentation getOne() {

    LcmId lcmId = lcmIdService.getLcmIdObject();
    if (lcmId == null) {
      throw new NotFoundException(String.format("LcmId not found."));
    }
    return new ConcreteLcmIdRepresentation(lcmId);
  }

  @DELETE
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response deleteLcmId() {
    lcmIdService.delete();
    return Response.ok().build();
  }
}
