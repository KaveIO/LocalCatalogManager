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

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import nl.kpmg.lcm.common.Roles;
import nl.kpmg.lcm.common.data.ContentIterator;
import nl.kpmg.lcm.common.data.Data;
import nl.kpmg.lcm.common.data.EnrichmentProperties;
import nl.kpmg.lcm.common.data.FetchEndpoint;
import nl.kpmg.lcm.common.data.IterativeData;
import nl.kpmg.lcm.common.data.StreamingData;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.common.exception.LcmExposableException;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.data.service.FetchEndpointService;
import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.rest.UserIdentifier;
import nl.kpmg.lcm.server.rest.authorization.PermissionChecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * @author S. Koulouzis
 */
@Path("/remote/v0/fetch")
@Api(value = "v0 Fetch Endpoint")
public class FetchEndpointController {
  private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("auditLogger");

  @Autowired
  private UserIdentifier userIdentifier;

  @Autowired
  private FetchEndpointService fetchEndpointService;

  @Autowired
  private MetaDataService metaDataService;

  @Autowired
  private StorageService storageService;
  
  @Autowired
  private PermissionChecker permissionChecker;

  @Context
  HttpServletRequest request;

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.REMOTE_USER})
  @ApiOperation(value = "Get authorized LCM with specified id.", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.REMOTE_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
       @ApiResponse(code = 400, message = "Fetch endpoint with specified id is expired!"),
       @ApiResponse(code = 403, message = "You are not authorized to access requested Fetch endpoint!"),
       @ApiResponse(code = 404, message = "Fetch endpoint with specified id is not found!")})
  public final Response getOne(@Context SecurityContext securityContext,
          @ApiParam( value = "Fetch endpoint Id") @PathParam("id") final String id, 
          @ApiParam( value = "Specify the exact data part which is requested") @QueryParam("data_key") String dataKey) throws URISyntaxException,
      IOException {
    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to get the fetch endpoint with id: " + id + ".");
    FetchEndpoint fe = fetchEndpointService.findOneById(id);

    if (fe == null) {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to get the fetch endpoint with id: " + id
          + " because such fetch endpoint is not found.");
      throw new NotFoundException(String.format("FetchEndpoint %s not found", id));
    }
    if (new Date(System.currentTimeMillis()).after(fe.getTimeToLive())) {
      fetchEndpointService.delete(fe);
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to get the fetch endpoint with id: " + id + " because it has expired.");
      throw new LcmExposableException(String.format("FetchEndpoint %s has expired", id),
          Response.Status.BAD_REQUEST);
    }
    if (!permissionChecker.check(securityContext, fe.getMetadataId())) {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to get the fetch endpoint with id: " + id
          + " because the user is not authorized to do so.");
      throw new ForbiddenException(String.format("Unable to authorize the request.", id));
    }

    MetaData md = metaDataService.findById(fe.getMetadataId());
    MetaDataWrapper metaDataWrapper = new MetaDataWrapper(md);
    if (metaDataWrapper.isEmpty()) {
      throw new LcmExposableException(String.format("Metadata %s not found", fe.getMetadataId()));
    }

    Backend backend = storageService.getBackend(metaDataWrapper);

    Data data = backend.read(dataKey);
    StreamingOutput result = new StreamingOutput() {
      @Override
      public void write(OutputStream out) throws IOException, WebApplicationException {
        AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
            + " is requesting enrichment of metadata with id: " + md.getId() + " and name: "
            + md.getName() + " because he wants to transfer it.");
        backend.enrichMetadata(EnrichmentProperties.createDefaultEnrichmentProperties());
        writeData(out, data);
        AUDIT_LOGGER.debug("Metadata with id: " + md.getId() + " and name: " + md.getName()
            + " is enriched successfully.");
      }
    };
    String mimeType = "application/json";
    Response.ResponseBuilder response = Response.ok(result, mimeType);
    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
        + " successfully got the fetch endpoint with id: " + id + ".");
    return response.build();
  }

  private void writeData(OutputStream stream, Data rawData) throws IOException {
    if (rawData instanceof IterativeData) {
      IterativeData data = (IterativeData) rawData;
      ContentIterator iter = data.getIterator();
      Gson gson = new Gson();
      try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(stream, "UTF-8"))) {
        writer.setIndent("  ");
        writer.beginArray();
        while (iter.hasNext()) {
          gson.toJson(iter.next(), Map.class, writer);
        }
        writer.endArray();
      }
    } else if (rawData instanceof StreamingData) {
      StreamingData data = (StreamingData) rawData;
      InputStream in = data.getInputStream();
      byte[] buffer = new byte[1024 * 1024];
      int readBytes;
      while ((readBytes = in.read(buffer)) != -1) {
        stream.write(buffer, 0, readBytes);
      }
    }
  }
}
