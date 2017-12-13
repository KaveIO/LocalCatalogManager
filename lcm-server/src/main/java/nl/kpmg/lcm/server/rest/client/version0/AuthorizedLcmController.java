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
import nl.kpmg.lcm.common.data.AuthorizedLcm;
import nl.kpmg.lcm.common.rest.types.AuthorizedLcmRepresentation;
import nl.kpmg.lcm.common.rest.types.AuthorizedLcmsRepresentation;
import nl.kpmg.lcm.server.data.service.AuthorizedLcmService;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteAuthorizedLcmRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteAuthorizedLcmsRepresentation;

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
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * @author shristov
 */
@Path("client/v0/authorizedlcm")
@Api(value = "v0 Authorized Lcm")
public class AuthorizedLcmController {
  private final int MAX_FIELD_LENTH = 128;

  @Autowired
  private AuthorizedLcmService authorizedLcmService;

  @GET
  @Path("{lcm_id}")
  @Produces({"application/nl.kpmg.lcm.common.rest.types.AuthorizedLcmRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Get authorized LCM with specified id.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
       @ApiResponse(code = 404, message = "Authorized Lcm with specified id is not found.")})
  public AuthorizedLcmRepresentation getAuthorizedLcm(@ApiParam( value = "Authorized LCM Id") @PathParam("lcm_id") String authorizedLcmId) {
    if (authorizedLcmId == null || authorizedLcmId.length() > MAX_FIELD_LENTH || authorizedLcmId.isEmpty()) {

      return null;
    }

    AuthorizedLcm authorizedLcm = authorizedLcmService.findOneById(authorizedLcmId);
    if(authorizedLcm ==  null) {
        throw new NotFoundException("Authorized Lcm with specified id is not found!");
    }

    return new ConcreteAuthorizedLcmRepresentation(authorizedLcm);
  }

  @GET
  @Produces({"application/nl.kpmg.lcm.common.rest.types.AuthorizedLcmsRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Get authorized LCM with specified id.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public AuthorizedLcmsRepresentation getAuthorizedLcmList() {
    List authroizedLcmList = authorizedLcmService.findAll();

    ConcreteAuthorizedLcmsRepresentation concreteAuthorizedLcmsRepresentation =
        new ConcreteAuthorizedLcmsRepresentation();

    concreteAuthorizedLcmsRepresentation.setRepresentedItems(
        ConcreteAuthorizedLcmRepresentation.class, authroizedLcmList);
    return concreteAuthorizedLcmsRepresentation;
  }

  @DELETE
  @Path("{lcm_id}")
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Get authorized LCM with specified id.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
       @ApiResponse(code = 404, message = "Authorized Lcm with specified id is not found.")})
  public Response deleteAuthorizedLcmHandler(@ApiParam( value = "Authorized LCM Id") final @PathParam("lcm_id") String authorizedLcmId) {
    if (authorizedLcmId == null || authorizedLcmId.length() > MAX_FIELD_LENTH || authorizedLcmId.isEmpty()) {

      return null;
    }

    AuthorizedLcm authorizedLcm = authorizedLcmService.findOneById(authorizedLcmId);
    if (authorizedLcm != null) {
      authorizedLcmService.delete(authorizedLcmId);
      return Response.ok().build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @POST
  @Consumes({"application/nl.kpmg.lcm.server.data.AuthorizedLcm+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Get authorized LCM with specified id.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
       @ApiResponse(code = 400, message = "Authorized Lcm has invalid data fields.")})
  public Response createNewAuthorizedLcm(final @ApiParam( value = "Authorized Lcm Object") AuthorizedLcm authorizedLcm) {
    if (authorizedLcm.getApplicationId() == null || authorizedLcm.getApplicationId().isEmpty()
        || authorizedLcm.getApplicationId().length() > MAX_FIELD_LENTH) {

      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Application ID could not be null or empty.").build();
    }

    if (authorizedLcm.getApplicationKey() == null || authorizedLcm.getApplicationKey().isEmpty()
        || authorizedLcm.getApplicationKey().length() > MAX_FIELD_LENTH) {

      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Application Key could not be null or empty.").build();
    }

    if (authorizedLcm.getUniqueId() == null || authorizedLcm.getUniqueId().isEmpty()
        || authorizedLcm.getUniqueId().length() > MAX_FIELD_LENTH) {

      return Response.status(Response.Status.BAD_REQUEST)
          .entity("LCM unique ID could not be null or empty.").build();
    }

    authorizedLcmService.create(authorizedLcm);
    return Response.ok().build();
  }

  @PUT
  @Consumes({"application/nl.kpmg.lcm.server.data.AuthorizedLcm+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Get authorized LCM with specified id.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
       @ApiResponse(code = 400, message = "Authorized Lcm has invalid data fields."),
       @ApiResponse(code = 404, message = "Can not be updated unexisting Authorized Lcm!")})
  public Response overwriteAuthorizedLcm(final AuthorizedLcm authorizedLcm) {
    if (authorizedLcm.getApplicationId() == null || authorizedLcm.getApplicationId().isEmpty()
        || authorizedLcm.getApplicationId().length() > MAX_FIELD_LENTH) {

      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Application ID could not be null or empty.").build();
    }
    
     if (authorizedLcm.getId() == null || authorizedLcm.getId().isEmpty()
        || authorizedLcm.getId().length() > MAX_FIELD_LENTH) {

      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Invalid update request. Authorized id is mandatory!").build();
    }

    if (authorizedLcm.getUniqueId() == null || authorizedLcm.getUniqueId().isEmpty()
        || authorizedLcm.getUniqueId().length() > MAX_FIELD_LENTH) {

      return Response.status(Response.Status.BAD_REQUEST)
          .entity("LCM unique ID could not be null or empty.").build();
    }
    AuthorizedLcm oldauthorizedLcm =  authorizedLcmService.findOneById(authorizedLcm.getId());
    if(oldauthorizedLcm ==  null) {
      return Response.status(Response.Status.NOT_FOUND)
              .entity("LCM with id" + authorizedLcm.getId() +
                      " can not be updaed as it does not exists could not be null or empty.")
              .build();
    }

    authorizedLcmService.update(authorizedLcm);
    return Response.ok().build();
  }


}
