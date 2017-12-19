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
import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.common.validation.Notification;
import nl.kpmg.lcm.server.data.service.RemoteDataDeletionService;
import nl.kpmg.lcm.server.rest.authorization.PermissionChecker;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * @author shristov
 */
@Path("client/v0/remoteData/")
@Api(value = "v0 delete data on Remote LCM")
public class RemoteDataDeletionController {

  @Autowired
  private RemoteDataDeletionService service;

  private final int MAX_FIELD_LENGTH = 128;

  @DELETE
  @Path("{metadata_id}")
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Delete data on Remote LCM.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final Response deleteActualData(@Context SecurityContext securityContext, @ApiParam(
      value = "Metadata Id") @PathParam("metadata_id") final String metadataId, @ApiParam(
      value = "Remote LCM Id") @QueryParam("lcmId") String lcmId) {

    validateRemoteLcmField(metadataId, "Metadata Id");
    validateRemoteLcmField(lcmId, "Remote Lcm ID");

    User principal = (User) securityContext.getUserPrincipal();
    PermissionChecker.getThreadLocal().set(principal);
    service.deleteRemoteData(lcmId, metadataId);

    return Response.ok().build();
  }

  private void validateRemoteLcmField(final String field, String fieldName) {
    if (field == null || field.isEmpty() || field.length() > MAX_FIELD_LENGTH) {
      Notification notification = new Notification();
      notification.addError(fieldName + " could not be null, empty or longer than "
          + MAX_FIELD_LENGTH + "!", null);
      throw new LcmValidationException(notification);
    }
  }
}