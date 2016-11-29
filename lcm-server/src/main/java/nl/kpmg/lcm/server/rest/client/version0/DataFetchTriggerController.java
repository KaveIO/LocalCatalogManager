/*
 * Copyright 2016 KPMG N.V. (unless otherwise stated).
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

import nl.kpmg.lcm.server.rest.authentication.Roles;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.service.DataFetchTriggerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author S. Koulouzis
 */
@Path("client/v0/remote")
public class DataFetchTriggerController {

  private final DataFetchTriggerService dataFetchTriggerService;

  @Autowired
  public DataFetchTriggerController(final DataFetchTriggerService dataFetchTriggerService) {
    this.dataFetchTriggerService = dataFetchTriggerService;
  }

  @POST
  @Path("{lcm_id}/metadata/{metadata_id}")
  @Produces({"text/plain"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public final Response trigger(@PathParam("lcm_id") final String lcmId,
          @PathParam("metadata_id") final String metadataId) throws ServerException {

    dataFetchTriggerService.scheduleDataFetchTask(lcmId, metadataId);

    return Response.ok().build();
  }
}
