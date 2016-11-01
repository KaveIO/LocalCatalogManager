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

import com.google.gson.stream.JsonWriter;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import nl.kpmg.lcm.server.rest.authentication.Roles;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import nl.kpmg.lcm.rest.types.FetchEndpointRepresentation;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.exception.BackendException;
import nl.kpmg.lcm.server.backend.exception.BackendNotImplementedException;
import nl.kpmg.lcm.server.backend.exception.BadMetaDataException;
import nl.kpmg.lcm.server.backend.exception.DataSourceValidationException;
import nl.kpmg.lcm.server.data.ContentIterator;
import nl.kpmg.lcm.server.data.Data;
import nl.kpmg.lcm.server.data.FetchEndpoint;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.dao.FetchEndpointDao;
import nl.kpmg.lcm.server.data.service.FetchEndpointService;
import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.data.service.exception.MissingStorageException;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteFetchEndpointRepresentation;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author S. Koulouzis
 */
@Path("/remote/v0")
public class FetchEndpointController {

  private final FetchEndpointService fEservice;
  private final MetaDataService metaDataService;
  private final StorageService storageService;

  @Context
  HttpServletRequest request;

  @Autowired
  public FetchEndpointController(final FetchEndpointService fEservice, final MetaDataService metaDataService,
          final StorageService storageService) {
    this.fEservice = fEservice;
    this.metaDataService = metaDataService;
    this.storageService = storageService;
  }

  @GET
  @Path("fetch/{id}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public final Response getOne(@PathParam("id") final String id)
          throws MissingStorageException, BadMetaDataException,
          DataSourceValidationException, BackendException, URISyntaxException, IOException {

    FetchEndpointDao dao = fEservice.getDao();
    FetchEndpoint fe = dao.findOneById(id);

    if (fe == null) {
      throw new NotFoundException(String.format("FetchEndpoint %s not found", id));
    }
    if (new Date(System.currentTimeMillis()).after(fe.getTimeToLive())) {
      fEservice.getDao().delete(fe);
      throw new NotFoundException(String.format("FetchEndpoint %s has expired", id));
    }
    MetaData md = metaDataService.getMetaDataDao().findOne(fe.getMetadataId());
    if (md == null) {
      throw new NotFoundException(String.format("Metadata %s not found", fe.getMetadataId()));
    }

    Backend backend = storageService.getBackend(md);

    Data data = backend.read();
    URI uri = new URI(md.getDataUri());
    String type = "json";//uri.getScheme();
    String name = FilenameUtils.getBaseName(uri.toString());
    StreamingOutput result = new StreamingOutput() {
      @Override
      public void write(OutputStream out) throws IOException, WebApplicationException {
       ContentIterator iter = data.getIterator();        
       Gson gson = new Gson();        
        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"))) {
          writer.setIndent("  ");
          writer.beginArray();
          while (iter.hasNext()) {
            gson.toJson(iter.next(), Map.class, writer);
          }
          writer.endArray();
        }
      }
    };
    String mimeType = "application/json";
    Response.ResponseBuilder response = Response.ok(result, mimeType);
    return response.header("Content-Disposition", "attachment; filename=" + name + "." + type).build();
  }

  @GET
  @Path("metadata/{metadata_id}/fetchUrl")
  @Produces({"application/nl.kpmg.lcm.rest.types.FetchEndpointRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public final FetchEndpointRepresentation generateFetch(@PathParam("metadata_id") final String metadata_id)
          throws BackendException, BackendNotImplementedException, MissingStorageException {

    MetaData md = metaDataService.getMetaDataDao().findOne(metadata_id);
    if (md == null) {
      throw new NotFoundException(String.format("Metadata %s not found", metadata_id));
    }

    //Not sure if this is necessary. Should we check if backend is up or contains the dataset?
//    Backend back = storageService.getBackend(md);
//    if (back == null) {
//      throw new NotFoundException(String.format("Backend holding metadata %s not found", metadata_id));
//    }
    FetchEndpoint fe = new FetchEndpoint();
    Date now = new Date();
    fe.setCreationDate(new Date());

    //We should read policies to detetermin the time to live. For now we set it one day
    Date later = getTimeToLive(now);

    fe.setTimeToLive(later);
    //Perhaps this is not needed. Since we have 2-way SSL and we are sure that the 
    //connection is secure then the token could be enough. 
    //If however we need added security we can put here a sgined token and keep it 
    // till data are consumebd. However this just reimplementing SSL handshake 
    fe.setUserToConsume("user");
    fe.setMetadataId(md.getId());
    fEservice.getDao().save(fe);

    return new ConcreteFetchEndpointRepresentation(fe);
  }

  /**
   * This is temporary. We should read policies.
   *
   * @param now
   * @return one day later
   */
  private Date getTimeToLive(Date now) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(now);
    calendar.add(Calendar.DAY_OF_YEAR, 1);
    return calendar.getTime();
  }

}
