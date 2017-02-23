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


import nl.kpmg.lcm.rest.types.RemoteLcmRepresentation;
import nl.kpmg.lcm.rest.types.RemoteLcmsRepresentation;
import nl.kpmg.lcm.server.data.RemoteLcm;
import nl.kpmg.lcm.server.data.dao.RemoteLcmDao;
import nl.kpmg.lcm.server.data.service.RemoteLcmService;
import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteRemoteLcmRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteRemoteLcmsRepresentation;
import nl.kpmg.lcm.validation.Notification;

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

/**
 *
 * @author mhoekstra, S. Koulouzis
 */
@Path("client/v0/remoteLcm")
public class RemoteLcmController {

  @Autowired
  private RemoteLcmService service;

  /**
   * Gets all registered lcms.
   *
   * @return the lcms
   */
  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.RemoteLcmsRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public RemoteLcmsRepresentation getAll() {
    List all = service.findAll();
    RemoteLcmsRepresentation lcmsRep = new ConcreteRemoteLcmsRepresentation();
    lcmsRep.setRepresentedItems(ConcreteRemoteLcmRepresentation.class, all);
    return lcmsRep;
  }

  /**
   * Gets lcm by id.
   *
   * @param id the id
   * @return the lcm
   */
  @GET
  @Path("{id}")
  @Produces({"application/nl.kpmg.lcm.rest.types.RemoteLcmRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public final RemoteLcmRepresentation getOne(@PathParam("id") final String id) {

    RemoteLcmDao dao = service.getDao();
    RemoteLcm lcm = dao.findOneById(id);
    if (lcm == null) {
      throw new NotFoundException(String.format("LCM %s not found", id));
    }
    return new ConcreteRemoteLcmRepresentation(lcm);
  }

  /**
   * Add lcm. Only admin can do that.
   *
   * @param lcm the lcm description
   * @return ok
   */
  @POST
  @Consumes({"application/nl.kpmg.lcm.server.data.RemoteLcm+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response add(final RemoteLcm lcm) {
    service.getDao().save(lcm);
    return Response.ok().build();
  }

  @PUT
  @Consumes({"application/nl.kpmg.lcm.server.data.RemoteLcm+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response update(RemoteLcm lcm) {
    if(lcm.getId() == null){
       Notification notification =new Notification();
       notification.addError("Id is null!");
       throw new LcmValidationException(notification);
    }

    service.getDao().save(lcm);
    return Response.ok().build();
  }

  /**
   * Delete LCM entry.
   *
   * @param id the id
   * @return 200 OK if successful
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response delete(@PathParam("id") final String id) {
    RemoteLcmRepresentation lcm = getOne(id);
    service.getDao().delete(lcm.getItem());
    return Response.ok().build();
  }

}
