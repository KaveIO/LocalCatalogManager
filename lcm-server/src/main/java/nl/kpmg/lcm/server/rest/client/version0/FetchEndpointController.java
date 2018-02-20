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
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.data.service.FetchEndpointService;
import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.rest.authorization.PermissionChecker;

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

/**
 *
 * @author S. Koulouzis
 */
@Path("/remote/v0/fetch")
public class FetchEndpointController {

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
  public final Response getOne(@Context SecurityContext securityContext,
          @PathParam("id") final String id, @QueryParam("data_key") String dataKey) throws URISyntaxException,
      IOException {
      
    FetchEndpoint fe = fetchEndpointService.findOneById(id);

    if (fe == null) {
      throw new NotFoundException(String.format("FetchEndpoint %s not found", id));
    }
    if (new Date(System.currentTimeMillis()).after(fe.getTimeToLive())) {
      fetchEndpointService.delete(fe);
      throw new LcmException(String.format("FetchEndpoint %s has expired", id),
          Response.Status.BAD_REQUEST);
    }
    if(!permissionChecker.check(securityContext, fe.getMetadataId())){
        throw new LcmException(String.format("Unable to authorize the request.", id),
          Response.Status.BAD_REQUEST);
    }

    MetaData md = metaDataService.findById(fe.getMetadataId());
    MetaDataWrapper metaDataWrapper = new MetaDataWrapper(md);
    if (metaDataWrapper.isEmpty()) {
      throw new LcmException(String.format("Metadata %s not found", fe.getMetadataId()));
    }

    Backend backend = storageService.getBackend(metaDataWrapper);

    Data data = backend.read(dataKey);
    StreamingOutput result = new StreamingOutput() {
      @Override
      public void write(OutputStream out) throws IOException, WebApplicationException {
        backend.enrichMetadata(EnrichmentProperties.createDefaultEnrichmentProperties());
        writeData(out, data);
      }
    };
    String mimeType = "application/json";
    Response.ResponseBuilder response = Response.ok(result, mimeType);
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
