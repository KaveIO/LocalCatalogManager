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
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.TestResult;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.common.rest.types.StorageRepresentation;
import nl.kpmg.lcm.common.rest.types.StoragesRepresentation;
import nl.kpmg.lcm.common.validation.Notification;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.rest.UserIdentifier;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteStorageRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteStoragesRepresentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
/**
 *
 * @author mhoekstra
 */
@Path("client/v0/storage")
@Api(value = "v0 storage")
public class StorageController {
  private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("auditLogger");

  @Autowired
  private UserIdentifier userIdentifier;

  private final StorageService storageService;

  @Autowired
  public StorageController(final StorageService storageService) {
    this.storageService = storageService;
  }

  /**
   * Get an overview of the connected storage handlers.
   *
   * @return
   */
  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.StoragesRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  @ApiOperation(value = "Return all the storages in the LCM.",  notes = "Roles: " + Roles.ADMINISTRATOR +", " + Roles.API_USER )
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public StoragesRepresentation getStorages(@Context SecurityContext securityContext) {
    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to access all of the local storages.");
    List all = storageService.findAll();
    ConcreteStoragesRepresentation concreteStoragesRepresentation =
        new ConcreteStoragesRepresentation();
    concreteStoragesRepresentation.setRepresentedItems(ConcreteStorageRepresentation.class, all);
    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
        + " accessed all of the local storages. Number of storages: " + all.size() + ".");
    return concreteStoragesRepresentation;
  }

  /**
   * Get an overview of the connected storage handlers.
   *
   * @param storageId
   * @return
   */
  @GET
  @Path("{storage_id}")
  @Produces({"application/nl.kpmg.lcm.rest.types.StoragesRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  @ApiOperation(value = "Return a storage with specified id.", notes = "Roles: " + Roles.ADMINISTRATOR +", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
       @ApiResponse(code = 404, message = "Storage with specified id is not found!")})
  public StorageRepresentation getStorageHandler(@Context SecurityContext securityContext,
      @ApiParam(value = "Storage Id") @PathParam("storage_id") String storageId) {
    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to access the storage with id: " + storageId + ".");

    Storage storage = storageService.findById(storageId);

    if (storage == null) {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to access the storage with id: " + storageId
          + " because such storage is not found.");
      throw new NotFoundException("Storage with specified id is not found!");
    }

    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
        + " accessed successfully the storage with name: " + storage.getName() + " and id: "
        + storage.getId() + ".");
    return new ConcreteStorageRepresentation(storage);
  }

  /**
   * Delete a specific storage handler.
   *
   * @param storageId
   * @return
   */
  @DELETE
  @Path("{storage_id}")
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Delete a storage with specified id.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
       @ApiResponse(code = 404, message = "The storage with specified id is not found.")})
  public Response deleteStorageHandler(@Context SecurityContext securityContext, final @ApiParam(
      value = "Storage Id") @PathParam("storage_id") String storageId) {
    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to delete the storage with id: " + storageId + ".");

    Storage storage = storageService.findById(storageId);
    if (storage != null) {
      storageService.delete(storage);
      AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, true)
          + " deleted successfully the storage with id: " + storage.getId() + " and name: "
          + storage.getName() + ".");
      return Response.ok().build();
    } else {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to delete the storage with id: " + storageId
          + " because such storage is not found.");
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  /**
   * Add new storage.
   *
   * @param storage
   * @return
   */
  @POST
  @Consumes({"application/nl.kpmg.lcm.server.data.Storage+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Create a storage.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response createNewStorage(@Context SecurityContext securityContext, @ApiParam(
      value = "Storage that will be created") final Storage storage) {
    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to create new storage with name: " + storage.getName() + " and type: "
        + storage.getType() + ".");

    storageService.add(storage);

    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, true)
        + " created successfully a storage with name: " + storage.getName() + " and id: "
        + storage.getId() + ".");
    return Response.ok().build();
  }

  /**
   * Update existing.
   *
   * @param storage
   * @return
   */
  @PUT
  @Consumes({"application/nl.kpmg.lcm.server.data.Storage+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Update a storage.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public Response overwriteStorage(@Context SecurityContext securityContext, @ApiParam(
      value = "Storage object.") final Storage storage) {
    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to update the storage with name: " + storage.getName() + " and id: "
        + storage.getId() + ".");

    Storage updatedStorage = storageService.update(storage);

    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, true)
        + " updated successfully the storage with name: " + updatedStorage.getName() + " and id: "
        + updatedStorage.getId() + ".");
    return Response.ok().build();
  }

  @GET
  @Path("status/{id}")
  @Produces({"application/nl.kpmg.lcm.rest.types.MetaDatasRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  @ApiOperation(value = "Test accessability of a storage.", notes = "Roles: " + Roles.ADMINISTRATOR +", " + Roles.API_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public TestResult getStorageStatus(@Context SecurityContext securityContext, @ApiParam(
      value = "Storage Id") @PathParam("id") final String id) {
    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to get the status of the storage with id: " + id + ".");

    if (id == null || id.isEmpty()) {
      Notification notification = new Notification();
      notification.addError("Id could not be null ot empty!", null);
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to get the status of the storage with id: " + id
          + " because the specified id is not valid.");
      throw new LcmValidationException(notification);
    }

    TestResult result = storageService.testStorage(id);

    AUDIT_LOGGER
        .debug(userIdentifier.getUserDescription(securityContext, true)
            + " accessed successfully the status of the storage with id: " + id + ". Status: "
            + result.getCode() + ".");
    return result;
  }

}