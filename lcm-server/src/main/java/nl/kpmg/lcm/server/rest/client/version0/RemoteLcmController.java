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
import nl.kpmg.lcm.common.data.RemoteLcm;
import nl.kpmg.lcm.common.data.TestResult;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.common.rest.types.RemoteLcmRepresentation;
import nl.kpmg.lcm.common.rest.types.RemoteLcmsRepresentation;
import nl.kpmg.lcm.common.validation.Notification;
import nl.kpmg.lcm.server.data.service.RemoteLcmService;
import nl.kpmg.lcm.server.data.service.TrustStoreService;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteRemoteLcmRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteRemoteLcmsRepresentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private final int MAX_FIELD_LENTH = 128;

  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteLcmController.class.getName());

  @Autowired
  private RemoteLcmService remoteLcmService;
  
  @Autowired
  private TrustStoreService trustStoreService;

  /**
   * Gets all registered lcms.
   *
   * @return the lcms
   */
  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.RemoteLcmsRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public RemoteLcmsRepresentation getAll() {
    List all = remoteLcmService.findAll();
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

    RemoteLcm lcm = remoteLcmService.findOneById(id);
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
  public final Response add(final RemoteLcm remoteLcm) {
    try {
      validateRemoteLcm(remoteLcm, true);
    } catch (LcmValidationException ex) {
      return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
    }

    remoteLcmService.create(remoteLcm);
    return Response.ok().build();
  }

  @PUT
  @Consumes({"application/nl.kpmg.lcm.server.data.RemoteLcm+json"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response update(RemoteLcm remoteLcm) {
    try {
      validateRemoteLcm(remoteLcm, false);
    } catch (LcmValidationException ex) {
      return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
    }
    remoteLcmService.update(remoteLcm);
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
  public final Response delete(@PathParam("id") final String remoteLcmId) {
    try {
      validateRemoteLcmField(remoteLcmId, "Remote LCM id");
    } catch (LcmValidationException ex) {
      return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
    }

    RemoteLcm remoteLcm = remoteLcmService.findOneById(remoteLcmId);
    if (remoteLcm != null) {
      remoteLcmService.delete(remoteLcmId);
      trustStoreService.removeCertificate(remoteLcm.getCertificateAlias());
      return Response.ok().build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @GET
  @Path("status/{id}")
  @Produces({"application/nl.kpmg.lcm.server.data.TestResult+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
  public TestResult getRemoteLcmStatus(@PathParam("id") final String remoteLcmId) {
    try {
      validateRemoteLcmField(remoteLcmId, "Remote LCM id");
    } catch (LcmValidationException ex) {
      LOGGER.warn("Can not test the connectivity of the remote LCM with id: " + remoteLcmId
          + ". Error message: " + ex.getMessage());
      return null;
    }
    return remoteLcmService.testRemoteLcmConnectivity(remoteLcmId);
  }

  @POST
  @Path("{id}/import-users")
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final Response importUsers(@PathParam("id") final String remoteLcmId) {
    try {
      validateRemoteLcmField(remoteLcmId, "Remote LCM id");
    } catch (LcmValidationException ex) {
      return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
    }

    remoteLcmService.importUsers(remoteLcmId);
    return Response.ok().build();
  }

  private void validateRemoteLcmField(final String field, String fieldName) {
    if (field == null || field.isEmpty() || field.length() > MAX_FIELD_LENTH) {
      Notification notification = new Notification();
      notification.addError(fieldName + " could not be null, empty or longer than "
          + MAX_FIELD_LENTH + "!", null);
      throw new LcmValidationException(notification);
    }
  }

  private void validateRemoteLcm(final RemoteLcm remoteLcm, boolean isRemoteLcmNew) {
    validateRemoteLcmField(remoteLcm.getName(), "Name");
    validateRemoteLcmField(remoteLcm.getProtocol(), "Protocol");
    validateRemoteLcmField(remoteLcm.getDomain(), "Domain");
    validateRemoteLcmField(remoteLcm.getPort().toString(), "Port");
    validateRemoteLcmField(remoteLcm.getApplicationId(), "Application id");
    if (isRemoteLcmNew) {
      validateRemoteLcmField(remoteLcm.getApplicationKey(), "Application key");
    }
  }
}
