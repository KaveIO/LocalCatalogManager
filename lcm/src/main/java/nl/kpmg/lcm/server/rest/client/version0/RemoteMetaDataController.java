/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.kpmg.lcm.server.rest.client.version0;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import nl.kpmg.lcm.server.AuthenticationManager;
import org.apache.commons.lang.NotImplementedException;

/**
 *
 * @author mhoekstra
 */
@Path("client/v0/remote")
public class RemoteMetaDataController {

    /**
     * Get an overview of the connected remote LCM's
     *
     * @return String "ok" as niceness
     */
    @GET
    @Produces({"application/json"})
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR, AuthenticationManager.Roles.API_USER})
    public String getRemote() {
        throw new NotImplementedException();
    }

    /**
     * Attach a remote LCM to your local LCM.
     *
     * @param remoteLcmName
     * @return
     */
    @PUT
    @Path("{remote_lcm_name}")
    @Produces({"application/json"})
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR})
    public String putRemoteLcmConnection(
            @PathParam("remote_lcm_name") String remoteLcmName) {
        throw new NotImplementedException();
    }

    /**
     * Get remote meta data overview
     *
     * @param remoteLcmName
     * @return
     */
    @GET
    @Path("{remote_lcm_name}")
    @Produces({"application/json"})
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR, AuthenticationManager.Roles.API_USER})
    public String getRemoteMetaDataOverview(
            @PathParam("remote_lcm_name") String remoteLcmName) {
        throw new NotImplementedException();
    }

    /**
     * Get meta data of the head version of a specific meta data set
     *
     * @param remoteLcmName
     * @param remoteMetaDataName
     * @return
     */
    @GET
    @Path("{remote_lcm_name}/{remote_meta_data}")
    @Produces({"application/json"})
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR, AuthenticationManager.Roles.API_USER})
    public String getRemoteMetaData(
            @PathParam("remote_lcm_name") String remoteLcmName,
            @PathParam("remote_meta_data") String remoteMetaDataName) {
        throw new NotImplementedException();
    }

    /**
     * Get meta data of a specific version of a specific meta data set
     *
     * @param remoteLcmName
     * @param remoteMetaDataName
     * @param version
     * @return
     */
    @GET
    @Path("{remote_lcm_name}/{remote_meta_data}/{version}")
    @Produces({"application/json"})
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR, AuthenticationManager.Roles.API_USER})
    public String getRemoteMetaDataByVersion(
            @PathParam("remote_lcm_name") String remoteLcmName,
            @PathParam("remote_meta_data") String remoteMetaDataName,
            @PathParam("version") String version) {
        throw new NotImplementedException();
    }

    /**
     * Attach the remote meta-data to your own LCM, This causes the meta data to
     * become available in like local meta-data.
     *
     * @param remoteLcmName
     * @param remoteMetaDataName
     * @param version
     * @return
     */
    @POST
    @Path("{remote_lcm_name}/{remote_meta_data}")
    @Produces({"application/json"})
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR, AuthenticationManager.Roles.API_USER})
    public String postRemoteAttachMetaData(
            @PathParam("remote_lcm_name") String remoteLcmName,
            @PathParam("remote_meta_data") String remoteMetaDataName,
            @PathParam("version") String version) {
        throw new NotImplementedException();
    }
}
