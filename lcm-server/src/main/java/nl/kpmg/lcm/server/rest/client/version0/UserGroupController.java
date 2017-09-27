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

import nl.kpmg.lcm.common.data.UserGroup;
import nl.kpmg.lcm.common.rest.types.UserGroupsRepresentation;
import nl.kpmg.lcm.server.data.service.UserGroupService;
import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteUserGroupRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteUserGroupsRepresentation;

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

@Component
@Path("client/v0/userGroups")
public class UserGroupController {

  private final UserGroupService userGroupService;

  @Autowired
  public UserGroupController(final UserGroupService userGroupService) {
    this.userGroupService = userGroupService;
  }

  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.UserGroupsRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final UserGroupsRepresentation getUserGroups() {
    List userGroups = userGroupService.findAll();
    ConcreteUserGroupsRepresentation concreteUserGroupsRepresentation =
        new ConcreteUserGroupsRepresentation();
    concreteUserGroupsRepresentation.setRepresentedItems(ConcreteUserGroupRepresentation.class,
        userGroups);

    return concreteUserGroupsRepresentation;
  }

  @GET
  @Path("/{user_group_id}")
  @Produces({"application/nl.kpmg.lcm.rest.types.UserGroupRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response getUserGroup(@PathParam("user_group_id") String userGroupId) {
    UserGroup userGroup = userGroupService.findOne(userGroupId);

    if (userGroup != null) {
      return Response.ok(new ConcreteUserGroupRepresentation(userGroup)).build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @POST
  @Consumes({"application/nl.kpmg.lcm.server.data.UserGroup+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response createNewUserGroup(final UserGroup userGroup) {
    userGroupService.save(userGroup);

    return Response.ok().build();
  }

  @PUT
  @Path("/{user_group_id}")
  @Consumes({"application/nl.kpmg.lcm.server.data.UserGroup+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response modifyUserGroup(@PathParam("user_group_id") final String userGroupId,
      final UserGroup userGroup) {
    userGroupService.save(userGroup);
    return Response.ok().build();
  }

  @DELETE
  @Path("/{user_group_id}")
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response deleteUserGroup(@PathParam("user_group_id") final String userGroupId) {

    UserGroup userGroup = userGroupService.findOne(userGroupId);
    if (userGroup != null) {
      userGroupService.delete(userGroup);
      return Response.ok().build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }
}
