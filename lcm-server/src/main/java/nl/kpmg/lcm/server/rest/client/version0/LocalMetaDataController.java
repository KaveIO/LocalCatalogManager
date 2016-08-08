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

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import nl.kpmg.lcm.rest.types.MetaDataOperationRequest;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendException;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.rest.types.MetaDataRepresentation;
import nl.kpmg.lcm.rest.types.MetaDatasRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteMetaDataRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteMetaDatasRepresentation;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author mhoekstra
 */
@Component
@Path("client/v0/local")
public class LocalMetaDataController {

  /**
   * The MetaDataService.
   */
  private final MetaDataService metaDataService;

  /**
   * The backend service.
   */
  private final StorageService storageService;

  /**
   * The default constructor.
   *
   * @param metaDataService for MetaData access
   * @param storageService for Backend access
   */
  @Autowired
  public LocalMetaDataController(final MetaDataService metaDataService,
      final StorageService storageService) {
    this.metaDataService = metaDataService;
    this.storageService = storageService;
  }

  /**
   * Get the head versions of all MetaData.
   *
   * @return The head versions
   */
  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.MetaDatasRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public final MetaDatasRepresentation getLocalMetaDataOverview() {
    List all = metaDataService.findAll();
    MetaDatasRepresentation metaDatasRepresentation = new ConcreteMetaDatasRepresentation();
    metaDatasRepresentation.setRepresentedItems(ConcreteMetaDataRepresentation.class, all);

    return metaDatasRepresentation;
  }

  /**
   * Create a new MetaData set.
   *
   * @param metaData first version of MetaData set to create
   * @return 200 OK if successful
   */
  @POST
  @Consumes({"application/nl.kpmg.lcm.server.data.MetaData+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public final Response createNewMetaData(final MetaData metaData) {
    metaDataService.getMetaDataDao().save(metaData);
    return Response.ok().build();
  }

  /**
   * Update a new MetaData item.
   *
   * @param metaDataId the name of the MetaData set
   * @param metaData the contents of the MetaData set
   * @return Response 200 OK if successful
   */
  @PUT
  @Path("{meta_data_id}")
  @Consumes({"application/nl.kpmg.lcm.server.data.MetaData+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public final Response putLocalMetaData(@PathParam("meta_data_id") final String metaDataId,
      final MetaData metaData) {

    metaData.setName(metaDataId);
    metaDataService.getMetaDataDao().save(metaData);

    return Response.ok().build();
  }

  @POST
  @Path("{meta_data_id}")
  @Consumes({"application/nl.kpmg.lcm.rest.types.MetaDataOperationRequest+json"})
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public final Response metadataOperation(@PathParam("meta_data_id") final String metaDataId,
      MetaDataOperationRequest request) throws BackendException, URISyntaxException {

    MetaData metadata = metaDataService.getMetaDataDao().findOne(metaDataId);

    Backend backend;
    switch (request.getOperation()) {

      case "download":

        backend = storageService.getBackend(metadata);
        try {
          InputStream input = backend.read(metadata);
          String fType = (String) request.getParameters().get("type");
          return Response.ok(input).header("Content-Disposition",
              String.format("attachment; filename=%s.%s", metadata.getName(), fType)).build();

        } catch (BackendException ex) {
          return Response.serverError().build();
        }
      case "copy":
        backend = storageService.getBackend(metadata);
        String fType = (String) request.getParameters().get("type");
        String sPath = (String) request.getParameters().get("storagePath");
        String fPath = (String) request.getParameters().get("Path");
        URI parsedURI;
        try {
          parsedURI = new URI(metadata.getDataUri());
          String newDataUri = parsedURI.getScheme() + "://" + parsedURI.getHost() + "/" + fPath;
          InputStream input = backend.read(metadata);
          FileOutputStream fos =
              new FileOutputStream(new File(String.format("%s/%s.%s", sPath, fPath, fType)));
          int copied = IOUtils.copy(input, fos);
          MetaData mnested = new MetaData();
          mnested.setDataUri(newDataUri);
          metadata.addDuplicate(mnested);
          metaDataService.getMetaDataDao().save(metadata);

          return Response.ok().build();
        } catch (IOException ex) {
          Logger.getLogger(LocalMetaDataController.class.getName()).log(Level.SEVERE,
              String.format("Couldn't find path: %s/%s.%s", sPath, fPath, fType), ex);
        }

    }

    return null;
  }

  /**
   * Get meta data of the head version of a specific meta data set.
   *
   * @param metaDataId The name of the meta data set
   * @return The head version of the requested meta data set
   */
  @GET
  @Path("{meta_data_id}")
  @Produces({"application/nl.kpmg.lcm.rest.types.MetaDataRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public final MetaDataRepresentation getLocalMetaData(
      @PathParam("meta_data_id") final String metaDataId) {

    MetaData metadata = metaDataService.getMetaDataDao().findOne(metaDataId);
    if (metadata == null) {
      throw new NotFoundException(String.format("MetaData set %s could not be found", metaDataId));
    }

    return new ConcreteMetaDataRepresentation(metadata);
  }

  /**
   * Delete the entire meta data set from the LCM.
   *
   * @param metaDataId The name of the meta data set
   * @return 200 OK if successful
   */
  @DELETE
  @Path("{meta_data_id}")
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public final Response deleteLocalMetaData(@PathParam("meta_data_id") final String metaDataId) {

    MetaData metadata = metaDataService.getMetaDataDao().findOne(metaDataId);
    if (metadata != null) {
      metaDataService.getMetaDataDao().delete(metadata);
      return Response.ok().build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }
}
