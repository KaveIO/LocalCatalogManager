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

import static nl.kpmg.lcm.common.Constants.MAX_INPUT_VALUE_LENGTH;

import nl.kpmg.lcm.common.Roles;
import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.common.rest.types.UsersRepresentation;
import nl.kpmg.lcm.common.validation.Notification;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.UserIdentifier;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteUserRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteUsersRepresentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
 * Controller for basic user operations, this also contains login in and logout.
 *
 * @author mhoekstra
 */
@Component
@Path("client/v0/users")
@Api(value = "v0 user")
public class UserController {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class.getName());

  private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("auditLogger");

  @Autowired
  private UserIdentifier userIdentifier;

  /**
   * The user service.
   */
  private final UserService userService;

  /**
   * Default constructor.
   *
   * @param userService providing user DAO access
   */
  @Autowired
  public UserController(final UserService userService) {
    this.userService = userService;
  }

  /**
   * Returns all the registered users in the system.
   *
   * @return list of all the users
   */
  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.UsersRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Return all the users.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final UsersRepresentation getUsers(@Context SecurityContext securityContext) {
    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to access all local users.");

    List users = userService.findAll();
    ConcreteUsersRepresentation concreteUsersRepresentation = new ConcreteUsersRepresentation();
    concreteUsersRepresentation.setRepresentedItems(ConcreteUserRepresentation.class, users);

    AUDIT_LOGGER
        .debug(userIdentifier.getUserDescription(securityContext, true)
            + " accessed successfully all local users. Number of users returned: " + users.size()
            + ".");
    return concreteUsersRepresentation;
  }

  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.UserRepresentation+json"})
  @Path("/{user_id}")
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Return single user.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                         @ApiResponse(code = 404, message = "The user is not found")})
  public final Response getUser(
          @Context SecurityContext securityContext,
          @ApiParam( value = "User id")
          @PathParam("user_id") String userId) {
    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to access the user with id: " + userId + ".");

    User user = userService.findById(userId);
    if (user != null) {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " accessed successfully the user with id: " + user.getId() + " and name: "
          + user.getName() + ".");
      return Response.ok(new ConcreteUserRepresentation(user)).build();
    } else {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to access the user with id: " + user.getId()
          + " because such user is not found.");
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @POST
  @Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Create a user.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final Response createNewUser(
 @Context SecurityContext securityContext, @ApiParam(
      value = "User object") final User user) {

    Notification notification = new Notification();
    if (user == null) {
      String message = "Paylaod could not be null. Please add user as payload!";
      notification.addError(message);
      LOGGER.debug(message);
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
          + " was not able to create new user because the user passed as paratemer is null.");
      throw new LcmValidationException(notification);
    }

    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to create new user with name: " + user.getName() + " and role: "
        + user.getRole() + ".");

    validateInputString(user.getName(), "user name", notification);
    validateInputString(user.getRole(), "user role", notification);
    if (!Roles.REMOTE_USER.equals(user.getRole())) {
      validateInputString(user.getNewPassword(), "user password", notification);
    }

    if (notification.hasErrors()) {
      LOGGER.debug(notification.errorMessage());
      AUDIT_LOGGER
          .debug(userIdentifier.getUserDescription(securityContext, true)
              + " was not able to create new user because at least one of its properties is not valid. "
              + notification.errorMessage());
      throw new LcmValidationException(notification);
    }

    User newUser = userService.create(user);

    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, true)
        + " created successfully new user with id: " + newUser.getId() + ", name: "
        + newUser.getName() + " and role: " + newUser.getRole() + ".");
    return Response.ok().build();
  }

  @PUT
  @Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Update a user.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final Response modifyUser(
          @Context SecurityContext securityContext,
          @ApiParam( value = "User object")
          final User user) {

    Notification notification = new Notification();
    if (user == null) {
      String message = "Paylaod could not be null. Please add user as payload!";
      notification.addError(message);
      LOGGER.debug(message);
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
          + " was not able to update the user because the user passed as paratemer is null.");
      throw new LcmValidationException(notification);
    }

    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to update the user with id: " + user.getId() + " and name: " + user.getName()
        + ".");

    User oldUser = userService.findById(user.getId());

    if (oldUser == null) {
      String message = "Can not update unexisting user!";
      notification.addError(message);
      LOGGER.debug(message);
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to update the user with id: " + user.getId() + " and name: "
          + user.getName() + " because such user is not found.");
      throw new LcmValidationException(notification);
    }

    validateUserFields(notification, oldUser, user);

    if (notification.hasErrors()) {
      LOGGER.debug(notification.errorMessage());
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was not able to update the user with id: " + user.getId() + " and name: "
          + user.getName() + " because at least one of the updated properties is not valid. "
          + notification.errorMessage());
      throw new LcmValidationException(notification);
    }

    User newUser = userService.update(user);

    setAuditLogForDifferingFields(securityContext, oldUser, newUser);

    return Response.ok().build();

  }

  @DELETE
  @Path("/{user_id}")
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Delete a user.", notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                         @ApiResponse(code = 404, message = "The user is not found")})
  public final Response deleteUser(
          @Context SecurityContext securityContext,
          @ApiParam( value = "User id")
          @PathParam("user_id") final String userId) {
    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to delete the user with id: " + userId + ".");

    User user = userService.findById(userId);

    if (user != null) {
      userService.delete(user);
      AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, true)
          + " deleted successfully the user with id: " + user.getId() + " and name: "
          + user.getName() + ".");
      return Response.ok().build();
    } else {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to delete the user with id: " + userId + ".");
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  private void validateInputString(final String value, final String fieldName,
      Notification notification) {
    if (value == null || value.length() == 0) {
      notification.addError("The " + fieldName + " could not be null or empty!");
    }

    if (value != null && value.length() > MAX_INPUT_VALUE_LENGTH) {
      notification.addError("The " + fieldName + " could not be longer then: "
          + MAX_INPUT_VALUE_LENGTH);
    }
  }

  private void validateUserFields(Notification notification, User oldUser, User user) {
    validateInputString(user.getName(), "user name", notification);
    validateInputString(user.getRole(), "user role", notification);
    if (Roles.REMOTE_USER.equals(oldUser.getRole()) && !user.getRole().equals(Roles.REMOTE_USER)) {

      String message = "Remote user's role could not be changed!";
      notification.addError(message);
    }

    if (Roles.REMOTE_USER.equals(user.getRole()) && Roles.ADMINISTRATOR.equals(user.getRole())
        && Roles.API_USER.equals(user.getRole())) {
      String message = "Invalid user role!";
      notification.addError(message);
    }

    if (Roles.REMOTE_USER.equals(oldUser.getRole()) && user.getNewPassword() != null
        && user.getNewPassword().length() >= 0) {

      String message = "Remote user can not have password!";
      notification.addError(message);
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
      return " Old " + fieldName + ": " + formatListAsString(oldValue) + ", new " + fieldName + ": "
          + formatListAsString(newValue) + ".";
    }
    return "";
  }

  private void setAuditLogForDifferingFields(SecurityContext securityContext, User oldUser,
      User newUser) {
    String messageForDifferentNames =
        getMessageForDifferingFields("name", oldUser.getName(), newUser.getName());

    String messageForDifferentRoles =
        getMessageForDifferingFields("role", oldUser.getRole(), newUser.getRole());

    String messageForDifferentOrigins =
        getMessageForDifferingFields("origin", oldUser.getOrigin(), newUser.getOrigin());

    String messageForDifferentListsOfMetadatas =
        getMessageForDifferingFields("list of allowed metadatas", oldUser.getAllowedMetadataList(),
            newUser.getAllowedMetadataList());

    String messageForDifferentListsOfPaths =
        getMessageForDifferingFields("list of allowed paths", oldUser.getAllowedPathList(),
            newUser.getAllowedPathList());

    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, true)
        + " updated successfully the user with id: " + newUser.getId() + " and name: "
        + newUser.getName() + "." + messageForDifferentNames + messageForDifferentRoles
        + messageForDifferentOrigins + messageForDifferentListsOfMetadatas
        + messageForDifferentListsOfPaths);
  }

}
