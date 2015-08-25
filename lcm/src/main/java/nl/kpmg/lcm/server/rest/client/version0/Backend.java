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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
@Path("client/v0/backend")
public class Backend {

    /**
     * Get an overview of the connected storage handlers.
     *
     * @return
     */
    @GET
    @Produces({"application/json"})
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR, AuthenticationManager.Roles.API_USER })
    public String getStorage() {
        throw new NotImplementedException();
    }

    /**
     * Get an overview of the connected storage handlers.
     *
     * @param storageHandlerName
     * @return
     */
    @GET
    @Path("{storage_handler_name}")
    @Produces({"application/json"})
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR, AuthenticationManager.Roles.API_USER })
    public String getStorageHandler(
            @PathParam("storage_handler_name") String storageHandlerName) {
        throw new NotImplementedException();
    }

    /**
     * Delete a specific storage handler.
     *
     * @param storageHandlerName
     * @return
     */
    @DELETE
    @Path("{storage_handler_name}")
    @Produces({"application/json"})
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR })
    public String deleteStorageHandler(
            @PathParam("storage_handler_name") String storageHandlerName) {
        throw new NotImplementedException();
    }

    /**
     * Add a specific storage handler.
     *
     * @param storageHandlerName
     * @return
     */
    @PUT
    @Path("{storage_handler_name}")
    @Produces({"application/json"})
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR })
    public String putStorageHandler(
            @PathParam("storage_handler_name") String storageHandlerName) {
        throw new NotImplementedException();
    }
}
