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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.kpmg.lcm.common.Roles;
import nl.kpmg.lcm.common.data.AuthorizedLcm;
import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.server.data.service.AuthorizedLcmService;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.UserIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
  private static final org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(RemoteLcmUserController.class.getName());

  private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("auditLogger");

  @Autowired
  private UserIdentifier userIdentifier;
  /**
   * The user service.
   */
  @Autowired
  private UserService userService;

  @Autowired
  private AuthorizedLcmService authorizedLcmService;

  @POST
  @Path("/import")
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.REMOTE_USER})
  @ApiOperation(value = "Import users from a remote LCM.", notes = "Roles: " + Roles.ADMINISTRATOR
      + ", " + Roles.REMOTE_USER)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final Response importUsers(@Context SecurityContext securityContext, @ApiParam(
      value = "Usernames map. Contains: \"usernames\".") Map payload) throws IOException {

    String usernamesJson = (String) payload.get("usernames");
    List<String> usernames = convertJsonToList(usernamesJson);

    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to import users: " + formatListAsString(usernames) + " to the local LCM.");

    User principal = (User) securityContext.getUserPrincipal();
    String remoteLcmId = principal.getOrigin();

    AuthorizedLcm authorizedLcm = authorizedLcmService.findOneByUniqueId(remoteLcmId);
    if (authorizedLcm == null) {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was not able to import users: " + formatListAsString(usernames)
          + " because the local LCM did not authorize LCM with id: " + remoteLcmId + ".");
      return Response.status(Response.Status.NOT_FOUND)
          .entity("Authorized LCM with id" + remoteLcmId + " can not be found.").build();
    }

    if (!authorizedLcm.isImportOfUsersAllowed()) {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was not able to import users: " + formatListAsString(usernames)
          + " because the remote LCM with id: " + remoteLcmId
          + " is not allowed to export its users to the local LCM.");
      return Response
          .status(Response.Status.BAD_REQUEST)
          .entity(
              "The remote LCM with id: " + remoteLcmId
                  + " is not allowed to export its users to the local LCM.").build();
    }

    userService.createRemoteUsers(usernames, remoteLcmId);

    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, true)
        + " imported successfully users: " + formatListAsString(usernames) + ".");
    return Response.status(Response.Status.OK).build();
  }

  public List<String> convertJsonToList(String usernamesJson) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(usernamesJson, new TypeReference<List<String>>() {});
  }

  private String formatListAsString(List<String> list) {
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
}