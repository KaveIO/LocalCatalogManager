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
package nl.kpmg.lcm.server.rest.client.version0;

import nl.kpmg.lcm.common.Roles;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.server.data.service.TrustStoreService;
import nl.kpmg.lcm.server.rest.UserIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
@Path("client/v0/truststore")
@Api(value = "v0  trust store)")
public class TrustStoreController {

  private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("auditLogger");

  private static final Logger LOGGER = LoggerFactory
      .getLogger(TrustStoreController.class.getName());

  private static int MAX_CERTIFICATE_SIZE = 1024 * 1024; // 100K

  @Autowired
  private UserIdentifier userIdentifier;

  @Autowired
  private TrustStoreService trustStoreService;

  @GET
  @RolesAllowed({Roles.ADMINISTRATOR})
  public final List<String> listAliases(@Context SecurityContext securityContext) {
    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to list all of the trust store aliases.");

    List<String> allAliases = trustStoreService.listTrustStoreAliases();

    AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
        + " listed successfully all of the trust store aliases. "
        + "Number of the trust store aliases: " + allAliases.size() + ".");
    return allAliases;
  }

  @POST
  @Path("/{alias}")
  @Consumes({"application/octet-stream"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Add Certificate(Public key) to the truststore.", 
          notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final Response addCertificate(
      @Context SecurityContext securityContext,
      @ApiParam(value = "Certificate alias. It must be unique within one LCM") @PathParam("alias") String alias,
      @ApiParam(value = "Stream to the certificate file.") InputStream certificateAsStream)
      throws IOException {
    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to add new certificate with alias: " + alias + ".");

    byte[] certificate = readCertificate(certificateAsStream);
    if (certificate.length > MAX_CERTIFICATE_SIZE) {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to add new certificate with alias: " + alias
          + " because the certificates could not be bigger than " + MAX_CERTIFICATE_SIZE
          + " bytes.");
      throw new LcmValidationException("Certificate can not be bigger then " + MAX_CERTIFICATE_SIZE
          + " bytes");
    }

    trustStoreService.addCertificate(certificate, alias);

    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, true)
        + " added successfully new certificate with alias: " + alias + ".");
    return Response.ok().build();
  }

  @PUT
  @Path("/{alias}")
  @Consumes({"application/octet-stream"})
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Update Certificate(Public key) to the truststore.", 
          notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final Response updateCertificate(
      @Context SecurityContext securityContext,
      @ApiParam(value = "Certificate alias. It must be unique within one LCM") @PathParam("alias") String alias,
      @ApiParam(value = "Stream to the certificate file.") InputStream certificateAsStream)
      throws IOException {
    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to update the certificate with alias: " + alias + ".");

    byte[] certificate = readCertificate(certificateAsStream);
    if(certificate == null) {
        return Response.status(Response.Status.BAD_REQUEST).entity("Unable to read a file!").build();
    }

    if (certificate.length > MAX_CERTIFICATE_SIZE) {
      AUDIT_LOGGER.debug(userIdentifier.getUserDescription(securityContext, true)
          + " was unable to update the certificate with alias: " + alias
          + " because the certificates could not be bigger than " + MAX_CERTIFICATE_SIZE
          + " bytes.");
      throw new LcmValidationException("Certificate can not be bigger then " + MAX_CERTIFICATE_SIZE
          + " bytes");
    }
    trustStoreService.removeCertificate(alias);
    trustStoreService.addCertificate(certificate, alias);

    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, true)
        + " updated successfully the certificate with alias: " + alias + ".");
    return Response.ok().build();
  }

  private byte[] readCertificate(InputStream certificateAsStream) throws IOException {


    byte[] data = new byte[MAX_CERTIFICATE_SIZE + 1];
    int nRead = certificateAsStream.read(data, 0, data.length);
    if( nRead == -1) {
        return null;
    }

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    buffer.write(data, 0, nRead);
    buffer.flush();

    return buffer.toByteArray();
  }

  @DELETE
  @Path("/{alias}")
  @RolesAllowed({Roles.ADMINISTRATOR})
  @ApiOperation(value = "Remove Certificate(Public key) from the truststore.", 
          notes = "Roles: " + Roles.ADMINISTRATOR)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  public final Response removeCertificate(
      @Context SecurityContext securityContext,
      @ApiParam(value = "Certificate alias. It must be unique within one LCM") @PathParam("alias") String alias) {
    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, false)
        + " is trying to remove the certificate with alias: " + alias + ".");

    trustStoreService.removeCertificate(alias);

    AUDIT_LOGGER.info(userIdentifier.getUserDescription(securityContext, true)
        + " removed successfully the certificate with alias: " + alias + ".");
    return Response.ok().build();
  }
}