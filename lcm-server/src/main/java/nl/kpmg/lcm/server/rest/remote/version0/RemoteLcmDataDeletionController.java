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

import nl.kpmg.lcm.common.Roles;
import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.TransferHistoryDescriptor;
import nl.kpmg.lcm.server.data.service.DataDeletionService;
import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.rest.UserIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
@Path("remote/v0/data")
@Api(value = "v0 remote calls for data")
public class RemoteLcmDataDeletionController {
  private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("auditLogger");

  @Autowired
  private UserIdentifier userIdentifier;

  @Autowired
  private MetaDataService metaDataService;

  @Autowired
  private DataDeletionService service;

  @DELETE
  @Path("{metadata_id}")
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.REMOTE_USER})
  @ApiOperation(value = "Delete data on the local LCM.", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.REMOTE_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                         @ApiResponse(code = 404, message = "The metadata is not found")})
  public final Response deleteActualData(@Context SecurityContext securityContext,
      @PathParam("metadata_id") final String metadataId) {
    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to delete the data behind the metadata with id: " + metadataId
        + " on the local LCM.");

    MetaData metadata = metaDataService.findById(metadataId);
    if (metadata == null) {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to delete the data behind the metadata with id: " + metadataId
          + " on the local LCM because such metadata is not found.");
      throw new NotFoundException(String.format("MetaData with id %s could not be found",
          metadataId));
    }

    User principal = (User) securityContext.getUserPrincipal();
    TransferHistoryDescriptor descriptor = new TransferHistoryDescriptor((metadata));
    List<String> transferHistory = descriptor.getTransferHistory();
    if (!principal.getOrigin().equals(transferHistory.get(transferHistory.size() - 1))) {
      String message =
          String
              .format(
                  "LCM with id: %s is not the last one that the metadata with id: %s had been transferred from.",
                  principal.getOrigin(), metadataId);
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
          + " was unable to delete the data behind the metadata with id: " + metadataId
          + " because " + message + ".");
      throw new ForbiddenException(message);
    }

    service.deleteDataByThread(metadata);

    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " deleted successfully the data behind the metadata with id: " + metadataId
        + " on the local LCM.");
    return Response.ok().build();
  }
}
