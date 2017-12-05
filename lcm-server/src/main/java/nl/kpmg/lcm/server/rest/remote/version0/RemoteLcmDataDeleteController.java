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
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.data.service.RemoteLcmDataDeleteService;
import nl.kpmg.lcm.server.rest.authorization.PermissionChecker;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author shristov
 */
@Path("remote/v0/delete")
public class RemoteLcmDataDeleteController {

  @Autowired
  private MetaDataService metaDataService;

  @Autowired
  private PermissionChecker permissionChecker;

  @Autowired
  private RemoteLcmDataDeleteService service;

  @DELETE
  @Path("{metadata_id}")
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.REMOTE_USER})
  public final Response deleteActualData(@Context SecurityContext securityContext,
      @PathParam("metadata_id") final String metadataId) {
    MetaData metadata = metaDataService.findById(metadataId);
    if (metadata == null) {
      throw new NotFoundException(String.format("MetaData with id %s could not be found",
          metadataId));
    }

    User principal = (User) securityContext.getUserPrincipal();
    TransferHistoryDescriptor descriptor = new TransferHistoryDescriptor((metadata));
    List<String> transferHistory = descriptor.getTransferHistory();
    if (!principal.getOrigin().equals(
        transferHistory.get(TransferHistoryDescriptor.MAX_SIZE_OF_TRANSFER_HISTORY - 1))) {
      throw new LcmException(String.format(
          "LCM with id: %s is not the last one that had transferred the metadata with id: %s.",
          principal.getOrigin(), metadataId), Response.Status.FORBIDDEN);
    }

    service.deleteData(metadata);

    return Response.ok().build();
  }
}