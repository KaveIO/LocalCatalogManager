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

import nl.kpmg.lcm.common.Roles;
import nl.kpmg.lcm.common.data.LcmId;
import nl.kpmg.lcm.common.rest.types.LcmIdRepresentation;
import nl.kpmg.lcm.server.data.service.LcmIdService;
import nl.kpmg.lcm.server.rest.UserIdentifier;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteLcmIdRepresentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * @author shristov
 */
@Path("client/v0/lcmId")
@Api(value = "v0 Lcm Id")
public class LcmIdController {
  private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("auditLogger");

  @Autowired
  private UserIdentifier userIdentifier;

  @Autowired
  private LcmIdService lcmIdService;

  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.LcmIdRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  @ApiOperation(value = "Get authorized LCM with specified id.", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
       @ApiResponse(code = 404, message = "Lcm id is not found!")})
  public final LcmIdRepresentation getOne(@Context SecurityContext securityContext) {
    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to access the local LCM id.");

    LcmId lcmId = lcmIdService.getLcmIdObject();
    if (lcmId == null) {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to access the local LCM id because it is not found.");
      throw new NotFoundException(String.format("Lcm id is not found."));
    }

    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
        + " accessed successfully the local LCM id: " + lcmId.getLcmId() + ".");
    return new ConcreteLcmIdRepresentation(lcmId);
  }

  @DELETE
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Get authorized LCM with specified id.", notes = "Roles: " + Roles.ADMINISTRATOR )
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
       @ApiResponse(code = 404, message = "Lcm id is not found!")})
  public final Response deleteLcmId(@Context SecurityContext securityContext) {
    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to delete the local LCM id: " + lcmIdService.getLcmIdObject().getLcmId() + ".");
    lcmIdService.delete();
    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " deleted successfully the local LCM id.");
    return Response.ok().build();
  }
}
