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
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;
import nl.kpmg.lcm.server.rest.client.version0.types.MetaDataRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.MetaDatasRepresentation;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author mhoekstra
 */
@Component
@Path("client/v0/local")
public class LocalMetaDataController {

    private MetaDataDao metaDataDao;

    @Autowired
    public LocalMetaDataController(MetaDataDao metaDataDao) {
        this.metaDataDao = metaDataDao;
    }

    /**
     * Get the head versions of all MetaData.
     *
     * @return The head versions
     */
    @GET
    @Produces({"application/json" })
    public final MetaDatasRepresentation getLocalMetaDataOverview() {
        List<MetaData> all = metaDataDao.getAll();
        return new MetaDatasRepresentation(all);
    }

    /**
     * Create a new MetaData set.
     *
     * @param metaData first version of MetaData set to create
     * @return 200 OK if successful
     */
    @POST
    @Consumes({"application/nl.kpmg.lcm.server.data.MetaData+json" })
    public final Response createNewMetaData(final MetaData metaData) {
        metaDataDao.persist(metaData);
        return Response.ok().build();
    }

    /**
     * Create a new MetaData item or version.
     *
     * @param metaDataName the name of the MetaData set
     * @param metaData the contents of the MetaData set
     * @return Response 200 OK if successful
     */
    @PUT
    @Path("{metaDataName}")
    @Consumes({"application/json" })
    @Produces(MediaType.TEXT_PLAIN)
    public final Response putLocalMetaData(
            @PathParam("metaDataName") final String metaDataName,
            final MetaData metaData) {

        metaData.setName(metaDataName);
        metaDataDao.persist(metaData);

        return Response.ok().build();
    }

    /**
     * Get meta data of the head version of a specific meta data set.
     *
     * @param metaDataName The name of the meta data set
     * @return The head version of the requested meta data set
     */
    @GET
    @Path("{metaDataName}")
    @Produces({"application/json" })
    public final MetaDataRepresentation getLocalMetaData(
             @PathParam("metaDataName") final String metaDataName) {

        MetaData metadata = metaDataDao.getByName(metaDataName);
        if (metadata == null) {
            throw new NotFoundException(String.format("MetaData set %s could not be found", metaDataName));
        }

        return new MetaDataRepresentation(metadata);
    }

    /**
     * Delete the entire meta data set from the LCM.
     *
     * @param metaDataName The name of the meta data set
     * @return 200 OK if successful
     */
    @DELETE
    @Path("{metaDataName}")
    @Produces({"application/json" })
    public final Response deleteLocalMetaData(
            @PathParam("metaDataName") final String metaDataName) {

        MetaData metadata = metaDataDao.getByName(metaDataName);
        if (metadata == null) {
            throw new NotFoundException(String.format("MetaData set %s could not be found", metaDataName));
        }

        metaDataDao.delete(metadata);

        return Response.ok().build();
    }

    /**
     * Place a fetch request for given data to be stored in a local data store.
     *
     * @param metaDataName
     * @param metaData
     * @return
     */
    @POST
    @Path("{metaDataName}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public final String postLocalMetaData(
            @PathParam("metaDataName") final String metaDataName,
            final MetaData metaData) {
        throw new NotImplementedException();
    }

    /**
     * Get meta data of a specific version of a specific meta data set.
     *
     * @param metaDataName The name of the meta data set
     * @param version The version of the meta data set
     * @return The head version of the requested meta data set
     */
    @GET
    @Path("{metaDataName}/{version}")
    @Produces({"application/json" })
    public final MetaDataRepresentation getLocalMetaDataByVersion(
            @PathParam("metaDataName") final String metaDataName,
            @PathParam("version") final String version) {

        MetaData metadata = metaDataDao.getByNameAndVersion(metaDataName, version);
        if (metadata == null) {
            throw new NotFoundException(String.format("MetaData set %s could not be found", metaDataName));
        }

        return new MetaDataRepresentation(metadata);
    }

    /**
     * Delete a version of meta data about a data-set.
     *
     * @param metaDataName
     * @param version
     * @return 200 OK if successful
     */
    @DELETE
    @Path("{metaDataName}/{version}")
    @Produces({"application/json" })
    public final Response deleteLocalMetaDataByVersion(
            @PathParam("metaDataName") final String metaDataName,
            @PathParam("version") final String version) {
        throw new NotImplementedException();
    }

    /**
     * Place a fetch request for given data to be stored in a local data store.
     *
     * @param metaDataName
     * @param version
     * @param metaData
     * @return 200 OK if successful
     */
    @POST
    @Path("{metadata}/{version}")
    @Consumes({"application/json" })
    @Produces({"application/json" })
    public final Response postLocalMetaDataByVersion(
            @PathParam("metaDataName") final String metaDataName,
            @PathParam("version") final String version,
            final MetaData metaData) {

        throw new NotImplementedException();
    }
}
