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

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.kpmg.lcm.common.client.ClientException;
import nl.kpmg.lcm.common.rest.types.MetaDatasRepresentation;
import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.data.TransferSettings;
import nl.kpmg.lcm.server.data.service.DataFetchTriggerService;
import nl.kpmg.lcm.server.data.service.RemoteMetaDataService;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.common.validation.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 *
 * @author mhoekstra
 */
@Component
@Path("client/v0/remote")
public class RemoteMetaDataController {
  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteMetaDataController.class.getName());

  @Autowired
  private DataFetchTriggerService dataFetchTriggerService;

  @Autowired
  private RemoteMetaDataService remoteMetaDataService;

  @POST
  @Path("{lcm_id}/metadata/{metadata_id}")
  @Produces({"text/plain"})
  @Consumes({"application/json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public final Response trigger(@PathParam("lcm_id") final String lcmId,
      @PathParam("metadata_id") final String metadataId, Map payload) throws ServerException {

    Notification notification = validateTriggerParams(payload, lcmId, metadataId);

    if (notification.hasErrors()) {
      throw new LcmValidationException(notification);
    }

    String localStorageId = (String) payload.get("local-storage-id");
    String namespacePath = (String) payload.get("namespace-path");

    String transferSettingsString = (String) payload.get("transfer-settings");

    TransferSettings transferSettings = null;
    if (transferSettingsString == null) {
        LOGGER.info("Unable to find TransferSettings object! Default settings will be used!");
      transferSettings = new TransferSettings();
    } else {
      try {
        ObjectMapper objectMapper = new ObjectMapper();
        transferSettings = objectMapper.readValue(transferSettingsString, TransferSettings.class);
      } catch (IOException iae) {
        LOGGER
            .warn("Unable to parse TransferSettings object, most probably it has invalid structure! Default settings will be used!");
        transferSettings = new TransferSettings();
      }
    }

    dataFetchTriggerService.scheduleDataFetchTask(lcmId, metadataId, localStorageId,
        transferSettings, namespacePath);

    return Response.ok().build();
  }

  private Notification validateTriggerParams(Map payload, final String lcmId,
      final String metadataId) {
    Notification notification = new Notification();
    if (payload == null || payload.isEmpty()) {
      notification.addError("Payload could not be null or empty!", null);
    } else if (payload.get("local-storage-id") == null
        || ((String) payload.get("local-storage-id")).isEmpty()) {
      notification.addError("Payload must contain valid \"local-storage-id\"!", null);
    }
    if (lcmId == null || lcmId.isEmpty()) {
      notification.addError("MetdataId could not be null or empty!", null);
    }
    if (metadataId == null || metadataId.isEmpty()) {
      notification.addError("MetdataId could not be null or empty!", null);
    }
    return notification;
  }

  @GET
  @Path("{scope}/search")
  // TODO Implement the actual custom LCM peer filtering
  @Produces({"application/nl.kpmg.lcm.rest.types.MetaDatasRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  public MetaDatasRepresentation searchMetadata(@PathParam("scope") final String scope,
      @QueryParam("text") String searchString) throws ServerException, ClientException {
    if (scope == null) {
      Notification notification = new Notification();
      notification.addError("Scope could not be null!", null);
      throw new LcmValidationException(notification);
    }

    if (searchString == null) {
      searchString = "";
    }

    MetaDatasRepresentation result =
        remoteMetaDataService.getMetaDatasRepresentation(scope, searchString);

    return result;
  }

}
