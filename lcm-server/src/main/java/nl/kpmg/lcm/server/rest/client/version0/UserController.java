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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Controller for basic user operations, this also contains login in and logout.
 *
 * @author mhoekstra
 */
@Component
@Path("client/v0/users")
public class UserController {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class.getName());

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
  public final UsersRepresentation getUsers() {
    List users = userService.findAll();
    ConcreteUsersRepresentation concreteUsersRepresentation = new ConcreteUsersRepresentation();
    concreteUsersRepresentation.setRepresentedItems(ConcreteUserRepresentation.class, users);

    return concreteUsersRepresentation;
  }

  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.UserRepresentation+json"})
  @Path("/{user_id}")
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response getUser(@PathParam("user_id") String userId) {
    User user = userService.findById(userId);
    if (user != null) {
      return Response.ok(new ConcreteUserRepresentation(user)).build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @POST
  @Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response createNewUser(final User user) {
    Notification notification = new Notification();
    if (user == null) {
      String message = "Paylaod could not be null. Please add user as payload!";
      notification.addError(message);
      LOGGER.debug(message);
      throw new LcmValidationException(notification);
    }

    validateInputString(user.getName(), "user name", notification);
    validateInputString(user.getRole(), "user role", notification);
    validateInputString(user.getNewPassword(), "user password", notification);

    if (notification.hasErrors()) {
      LOGGER.debug(notification.errorMessage());
      throw new LcmValidationException(notification);
    }
    userService.create(user);
    return Response.ok().build();
  }

  @PUT
  @Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response modifyUser(final User user) {
    Notification notification = new Notification();
    if (user == null) {
      String message = "Paylaod could not be null. Please add user as payload!";
      notification.addError(message);
      LOGGER.debug(message);
      throw new LcmValidationException(notification);
    }

    User oldUser = userService.findById(user.getId());

    if (oldUser == null) {
      String message = "Can not update unexisting user!";
      notification.addError(message);
      LOGGER.debug(message);
      throw new LcmValidationException(notification);
    }

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
        && user.getNewPassword().length() == 0 ) {

      String message = "Invalid password!";
      notification.addError(message);
    }


    if (notification.hasErrors()) {
      LOGGER.debug(notification.errorMessage());
      throw new LcmValidationException(notification);
    }

    userService.update(user);
    return Response.ok().build();

  }

  @DELETE
  @Path("/{user_id}")
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response deleteUser(@PathParam("user_id") final String userId) {
    User user = userService.findById(userId);

    if (user != null) {
      userService.delete(user);
      return Response.ok().build();
    } else {
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
}
