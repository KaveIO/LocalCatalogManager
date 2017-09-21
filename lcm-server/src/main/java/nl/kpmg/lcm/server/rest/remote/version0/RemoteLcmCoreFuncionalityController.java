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

import nl.kpmg.lcm.server.rest.authentication.Roles;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 *
 * @author shristov
 */
@Component
@Path("remote/v0/core")
public class RemoteLcmCoreFuncionalityController {

  private String customId;


  @Value("${lcm.server.custom.id}")
  public final void setUniqueId(final String customId) {
    this.customId = customId;
  }

  @GET
  @Path("/id")
  @RolesAllowed({Roles.ANY_USER})
  public final String getLCMId() {

    //TODO: need conception how to generate it and store it
    //most probably we wiillne ed this custom id and will add current time im millis
    // and some random number/string. after that i must be saved and never generated again
    //unitil the DB regord exists.
    return customId;
  }
}
