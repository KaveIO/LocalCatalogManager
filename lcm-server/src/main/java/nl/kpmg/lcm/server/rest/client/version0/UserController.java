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

import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.rest.authentication.UserPasswordHashException;
import nl.kpmg.lcm.common.rest.types.UsersRepresentation;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.authentication.Roles;
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
    if (user.getOrigin() == null) {
      user.setOrigin(User.LOCAL_ORIGIN);
    }

    userService.save(user);
    return Response.ok().build();
  }

  @PUT
  @Path("/{user_id}")
  @Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response modifyUser(@PathParam("user_id") final String userId, final User user) {

    try {
      userService.updateUser(userId, user);
      return Response.ok().build();
    } catch (UserPasswordHashException ex) {
      LOGGER.error("Password hashing failed during user modification", ex);
      return Response.serverError().build();
    }
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
}
