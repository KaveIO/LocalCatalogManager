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

import nl.kpmg.lcm.common.Roles;
import nl.kpmg.lcm.common.data.UserGroup;
import nl.kpmg.lcm.common.rest.types.UserGroupsRepresentation;
import nl.kpmg.lcm.server.data.service.UserGroupService;
import nl.kpmg.lcm.server.rest.UserIdentifier;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteUserGroupRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteUserGroupsRepresentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

@Component
@Path("client/v0/userGroups")
@Api(value = "v0 user groups")
public class UserGroupController {
  private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("auditLogger");

  @Autowired
  private UserIdentifier userIdentifier;

  private final UserGroupService userGroupService;

  @Autowired
  public UserGroupController(final UserGroupService userGroupService) {
    this.userGroupService = userGroupService;
  }

  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.UserGroupsRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Get all user groups.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final UserGroupsRepresentation getUserGroups(
        @Context SecurityContext securityContext) {
    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to access all of the user groups.");

    List userGroups = userGroupService.findAll();
    ConcreteUserGroupsRepresentation concreteUserGroupsRepresentation =
        new ConcreteUserGroupsRepresentation();
    concreteUserGroupsRepresentation.setRepresentedItems(ConcreteUserGroupRepresentation.class,
        userGroups);

    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
        + " accessed successfully all of the user groups. Number of user groups returned: "
        + userGroups.size() + ".");
    return concreteUserGroupsRepresentation;
  }

  @GET
  @Path("/{user_group_id}")
  @Produces({"application/nl.kpmg.lcm.rest.types.UserGroupRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Get a single user group.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                         @ApiResponse(code = 404, message = "The user is not found")})
  public final Response getUserGroup(
          @Context SecurityContext securityContext,
          @ApiParam( value = "User group id")
          @PathParam("user_group_id") String userGroupId) {
    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to get access the user group with id: " + userGroupId + ".");

    UserGroup userGroup = userGroupService.findOne(userGroupId);

    if (userGroup != null) {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " accessed successfully the user group with id: " + userGroup.getId() + " and name: "
          + userGroup.getName() + ".");
      return Response.ok(new ConcreteUserGroupRepresentation(userGroup)).build();
    } else {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to access the user group with id: " + userGroupId
          + " because such user group is not found.");
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @POST
  @Consumes({"application/nl.kpmg.lcm.server.data.UserGroup+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Create a user group.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final Response createNewUserGroup(
          @Context SecurityContext securityContext,
          @ApiParam( value = "User group object")
          final UserGroup userGroup) {
    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to create new user group with name: " + userGroup.getName() + ".");

    UserGroup newUserGroup = userGroupService.save(userGroup);

    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, true)
        + " successfully created new user group with name: " + newUserGroup.getName() + " and id: "
        + newUserGroup.getId() + ".");
    return Response.ok().build();
  }

  @PUT
  @Consumes({"application/nl.kpmg.lcm.server.data.UserGroup+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
    @ApiOperation(value = "Update a user group.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final Response modifyUserGroup(
          @Context SecurityContext securityContext,
          @ApiParam( value = "User group object")
          final UserGroup userGroup) {
    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to update the user group with id: " + userGroup.getId() + " and name: "
        + userGroup.getName() + ".");

    UserGroup oldUserGroup = userGroupService.findOne(userGroup.getId());
    if (oldUserGroup == null) {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to update the user group with id: " + userGroup.getId()
          + " because such user group is not found.");
      throw new NotFoundException("User group with specified id is not found!");
    }

    UserGroup newUserGroup = userGroupService.save(userGroup);

    setAuditLogForDifferingFields(securityContext, oldUserGroup, newUserGroup);

    return Response.ok().build();
  }

  @DELETE
  @Path("/{user_group_id}")
  @RolesAllowed({Roles.ADMINISTRATOR})
    @ApiOperation(value = "Delete a user group.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                         @ApiResponse(code = 404, message = "The user group is not found")})
  public final Response deleteUserGroup(
          @Context SecurityContext securityContext,
          @ApiParam( value = "User group id")
          @PathParam("user_group_id") final String userGroupId) {
    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to delete the user group with id: " + userGroupId + ".");

    UserGroup userGroup = userGroupService.findOne(userGroupId);
    if (userGroup != null) {
      userGroupService.delete(userGroup);
      AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, true)
          + " deleted successfully the user group with id: " + userGroup.getId() + " and name: "
          + userGroup.getName() + ".");
      return Response.ok().build();
    } else {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to delete the user group with id: " + userGroupId
          + " because such user group is not found.");
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  private String formatListAsString(List<String> list) {
    if (list == null || list.size() == 0) {
      return "";
    }

    StringBuilder builder = new StringBuilder();

    for (String element : list) {
      builder.append(element + ", ");
    }

    // Remote the last empty space
    builder.deleteCharAt(builder.length() - 1);
    // Remote the last comma character
    builder.deleteCharAt(builder.length() - 1);

    return builder.toString();
  }

  private String getMessageForDifferingFields(String fieldName, String oldValue, String newValue) {
    if (oldValue == null && newValue == null)
      return "";

    if (oldValue == null || !oldValue.equals(newValue)) {
      return " Old " + fieldName + ": " + oldValue + ", new " + fieldName + ": " + newValue + ".";
    }
    return "";
  }

  private String getMessageForDifferingFields(String fieldName, List<String> oldValue,
      List<String> newValue) {
    if ((oldValue == null || oldValue.size() == 0) && (newValue == null || newValue.size() == 0))
      return "";

    if (oldValue == null || !oldValue.equals(newValue)) {
      return " Old " + fieldName + ": " + formatListAsString(oldValue) + ", new " + fieldName
          + ": " + formatListAsString(newValue) + ".";
    }
    return "";
  }

  private void setAuditLogForDifferingFields(SecurityContext securityContext,
      UserGroup oldUserGroup, UserGroup newUserGroup) {

    String messageForDifferentNames =
        getMessageForDifferingFields("name", oldUserGroup.getName(), newUserGroup.getName());

    String messageForDifferentListsOfUsers =
        getMessageForDifferingFields("list of user ids", oldUserGroup.getUsers(),
            newUserGroup.getUsers());

    String messageForDifferentListsOfMetadatas =
        getMessageForDifferingFields("list of allowed metadatas",
            oldUserGroup.getAllowedMetadataList(), newUserGroup.getAllowedMetadataList());

    String messageForDifferentListsOfPaths =
        getMessageForDifferingFields("list of allowed paths", oldUserGroup.getAllowedPathList(),
            newUserGroup.getAllowedPathList());

    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, true)
        + " updated successfully the user group with id: " + newUserGroup.getId() + " and name: "
        + newUserGroup.getName() + "." + messageForDifferentNames + messageForDifferentListsOfUsers
        + messageForDifferentListsOfMetadatas + messageForDifferentListsOfPaths);
  }
}
