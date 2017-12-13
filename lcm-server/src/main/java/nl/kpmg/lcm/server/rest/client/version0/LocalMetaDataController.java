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

import nl.kpmg.lcm.common.Roles;
import nl.kpmg.lcm.common.data.EnrichmentProperties;
import nl.kpmg.lcm.common.data.IterativeData;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.common.rest.types.MetaDataOperationRequest;
import nl.kpmg.lcm.common.rest.types.MetaDataRepresentation;
import nl.kpmg.lcm.common.rest.types.MetaDatasRepresentation;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteMetaDataRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteMetaDatasRepresentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * @author mhoekstra
 */
@Component
@Path("client/v0/local")
@Api(value = "v0 local metadata")
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
   * @param namespace to filter metadata on. default empty
   * @param recursive get all underlying metadata from namespace. default false
   * @return The head versions
   */
  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.MetaDatasRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  @ApiOperation(value = "Get metadata list.", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final MetaDatasRepresentation getLocalMetaDataOverview(
      @ApiParam( value = "Retern all the metadatas from specified namespace.") 
      @QueryParam("namespace") @DefaultValue("") String namespace,
      @ApiParam( value = "Retern all the metadatas from specified namespace and subspaces.")
      @QueryParam("recursive") @DefaultValue("False") Boolean recursive) {

    List all;
    if (!namespace.isEmpty()) {
      all = metaDataService.findAllByNamespace(namespace, recursive);
    } else {
      all = metaDataService.findAll();
    }
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
  @ApiOperation(value = "Create metadata.", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final Response createNewMetaData(final @ApiParam( value = "Metadata Object") MetaData metaData) {
    new MetaDataWrapper(metaData);// validate that MetaData has correct format
    metaDataService.create(metaData);
    return Response.ok().build();
  }

  /**
   * Update a new MetaData item.
   *
   * @param metaDataId the name of the MetaData set
   * @param metadata the contents of the MetaData set
   * @return Response 200 OK if successful
   */
  @PUT
  @Path("{meta_data_id}")
  @Consumes({"application/nl.kpmg.lcm.server.data.MetaData+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  @ApiOperation(value = "Update metadata", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final Response putLocalMetaData(
          @ApiParam( value = "Metadata id")
          @PathParam("meta_data_id") final String metaDataId,
          @ApiParam( value = "Metadata Object")  final MetaData metadata) {

    new MetaDataWrapper(metadata);// validate that MetaData has correct format
    metaDataService.update(metadata);

    return Response.ok().build();
  }

  @POST
  @Path("{meta_data_id}")
  @Consumes({"application/nl.kpmg.lcm.rest.types.MetaDataOperationRequest+json"})
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  @ApiOperation(value = "Metadata operation.", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final Response metadataOperation(
          @ApiParam( value = "Metadata id")
          @PathParam("meta_data_id") final String metaDataId, 
          @ApiParam( value = "Data key that specifies processed data item") @QueryParam("data_key") String dataKey,
      MetaDataOperationRequest request) {

    MetaData metadata = metaDataService.findById(metaDataId);
    MetaDataWrapper metaDataWrapper = new MetaDataWrapper(metadata);
    Backend backend = null;
    switch (request.getOperation()) {

      case "download":

        try {
          backend = storageService.getBackend(metaDataWrapper);
          if (backend != null) {
            IterativeData input = (IterativeData) backend.read(dataKey);
            String fType = (String) request.getParameters().get("type");
            return Response.ok(input).header("Content-Disposition",
                String.format("attachment; filename=%s.%s", metadata.getName(), fType)).build();

          }
        } finally {
          if (backend != null) {
            backend.free();
          }
        }
      case "copy":
    }

    return null;
  }

  @POST
  @Path("{meta_data_id}/enrich")
  @Consumes({"application/nl.kpmg.lcm.server.data.EnrichmentProperties+json"})
  @Produces({"application/nl.kpmg.lcm.rest.types.MetaDataRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  @ApiOperation(value = "Force metadata enrichment.", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                         @ApiResponse(code = 404, message = "Metadata is not found!")})
  public final MetaDataRepresentation metadataEnrichment(
      @ApiParam( value = "Metadata id") @PathParam("meta_data_id") final String metaDataId, 
      @ApiParam( value = "Enrichment properties") EnrichmentProperties properties) {
      
    MetaData metadata = metaDataService.findById(metaDataId);
    if (metadata == null) {
      throw new NotFoundException(String.format("MetaData set %s could not be found", metaDataId));
    }

    MetaDataWrapper metaDataWrapper = new MetaDataWrapper(metadata);
    if(properties == null){
        properties = EnrichmentProperties.createDefaultEnrichmentProperties();
    }
    boolean result = metaDataService.enrichMetadata(metaDataWrapper, properties);
    if (result) {
      MetaData updatedMetadata = metaDataService.findById(metaDataId);
      return new ConcreteMetaDataRepresentation(updatedMetadata);
    } else {
      throw new LcmException("Unable to enrich the metadata. Id: " + metaDataId);
    }
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
  @ApiOperation(value = "Return metadata.", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                         @ApiResponse(code = 404, message = "Metadata is not found!")})
  public final MetaDataRepresentation getLocalMetaData(
     @ApiParam( value = "Metadata id")  
     @PathParam("meta_data_id") final String metaDataId, 
     @ApiParam( value = "If true the metadata is enriched and then returned.") 
     @QueryParam("update") Boolean update) {

    MetaData metadata = metaDataService.findById(metaDataId);
    if (metadata == null) {
      throw new NotFoundException(String.format("MetaData set %s could not be found", metaDataId));
    }

    if (update) {
      Backend backend = storageService.getBackend(new MetaDataWrapper(metadata));
      metadata = backend.enrichMetadata(EnrichmentProperties.createDefaultEnrichmentProperties());
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
  @ApiOperation(value = "Delete metadata", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                         @ApiResponse(code = 404, message = "Metadata is not found!")})
  public final Response deleteLocalMetaData(
          @ApiParam( value = "Metadata id")  
          @PathParam("meta_data_id") final String metaDataId) {

    MetaData metadata = metaDataService.findById(metaDataId);
    if (metadata != null) {
      metaDataService.delete(metadata);
      return Response.ok().build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @GET
  @Path("namespace")
  @Produces({"application/json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
    @ApiOperation(value = "Return namespaces in the Lcm", notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                         @ApiResponse(code = 404, message = "Metadata is not found!")})
  public final Set<String> getSubNamespaces(
      @ApiParam( value = "Base namespace.") 
      @QueryParam("namespace") String namespace,
      @ApiParam( value = "Wheather to list namespaces recursively.") 
      @QueryParam("recursive") Boolean recursive) {

    if (namespace != null && !namespace.isEmpty()) {
      if (recursive == null) {
        recursive = Boolean.FALSE;
      }
      return metaDataService.getAllSubNamespaces(namespace, recursive);
    }
    return new HashSet<String>();
  }
}
