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
package nl.kpmg.lcm.server.rest.remote.version0;

import nl.kpmg.lcm.common.Roles;
import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.server.data.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * @author shristov
 */
@Component
@Path("remote/v0/users")
@Api(value = "v0 remote calls for users")
public class RemoteLcmUserController {

  /**
   * The user service.
   */
  @Autowired
  private UserService userService;

  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.UsersRepresentation+json"})
  @Path("/username-list")
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.REMOTE_USER})
  @ApiOperation(value = "Return list of usernames of local users.",
          notes = "Roles: " + Roles.ADMINISTRATOR + ", " + Roles.REMOTE_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final List<String> getUsernames() {
    List<User> users = userService.findAll();
    List<String> usernameList = new LinkedList();
    for(User user : users) {
        usernameList.add(user.getName());
    }

    return usernameList;
  }

}
