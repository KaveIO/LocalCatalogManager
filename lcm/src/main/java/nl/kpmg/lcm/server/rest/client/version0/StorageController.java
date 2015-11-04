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

import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.dao.StorageDao;
import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.server.rest.client.version0.types.StorageRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.StoragesRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author mhoekstra
 */
@Path("client/v0/storage")
public class StorageController {

    private final StorageDao storageDao;

    @Autowired
    public StorageController(final StorageDao storageDao) {
        this.storageDao = storageDao;
    }

    /**
     * Get an overview of the connected storage handlers.
     *
     * @return
     */
    @GET
    @Produces({"application/nl.kpmg.lcm.server.rest.client.version0.types.StoragesRepresentation+json"})
    @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
    public StoragesRepresentation getStorage() {
        List<Storage> all = storageDao.getAll();
        return new StoragesRepresentation(all);
    }

    /**
     * Get an overview of the connected storage handlers.
     *
     * @param storageId
     * @return
     */
    @GET
    @Path("{storage_id}")
    @Produces({"application/nl.kpmg.lcm.server.rest.client.version0.types.StoragesRepresentation+json"})
    @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
    public StorageRepresentation getStorageHandler(
            @PathParam("storage_id") String storageId) {
        Storage storage = storageDao.getById(storageId);
        return new StorageRepresentation(storage);
    }

    /**
     * Delete a specific storage handler.
     *
     * @param storageId
     * @return
     */
    @DELETE
    @Path("{storage_id}")
    @RolesAllowed({Roles.ADMINISTRATOR})
    public Response deleteStorageHandler(
            final @PathParam("storage_id") String storageId) {

        Storage storage = storageDao.getById(storageId);
        if (storage != null) {
            storageDao.delete(storage);
            return Response.ok().build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    /**
     * Add a specific storage handler.
     *
     * @param storage
     * @return
     */
    @POST
    @Consumes({"application/nl.kpmg.lcm.server.data.Storage+json"})
    @RolesAllowed({Roles.ADMINISTRATOR})
    public Response createNewStorage(final Storage storage) {
        storageDao.persist(storage);
        return Response.ok().build();
    }
}