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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import nl.kpmg.lcm.server.Resources;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;
import nl.kpmg.lcm.server.rest.client.MetaDataNotFoundException;
import org.apache.commons.lang.NotImplementedException;

/**
 *
 * @author mhoekstra
 */
@Path("client/v0/local")
public class Local {

    private Map getHATEOASLink(final String relation, final String href) {
        return new HashMap() { {
            put("rel", relation);
            put("href", href);
        } };
    }

    private void fillHATEOASLinks(MetaData metadata) {
        LinkedList list = new LinkedList();
        list.add(getHATEOASLink("self", ""));
        metadata.put("links", list);
    }

    /**
     * Get local meta data overview
     *
     * @return
     */
    @GET
    @Produces({"application/json"})
    public Map getLocalMetaDataOverview() {
        HashMap result = new HashMap();

        MetaDataDao metaDataDao = Resources.getMetaDataDao();
        for (MetaData metadata : metaDataDao.getAll()) {
            HashMap details = new HashMap();
            details.put("url", "http://localhost:8080/client/v0/local/" + metadata.getName() + "/" + metadata.getVersionNumber());
            result.put(metadata.getName(), details);
        }
        return result;
    }

    /**
     * Create a new metadata item.
     *
     * @param metaDataName
     * @param metaData
     * @return String "ok" if call is successful
     */
    @PUT
    @Path("{metaDataName}")
    @Consumes({"application/json"})
    @Produces(MediaType.TEXT_PLAIN)
    public String putLocalMetaData(
            @PathParam("metaDataName") String metaDataName,
            MetaData metaData) {

        metaData.setName(metaDataName);

        MetaDataDao metaDataDao = Resources.getMetaDataDao();
        metaDataDao.persist(metaData);

        return "ok";
    }

    /**
     * Get meta data of the head version of a specific meta data set.
     *
     * @param metaDataName The name of the meta data set
     * @return The head version of the requested meta data set
     * @throws MetaDataNotFoundException
     */
    @GET
    @Path("{metaDataName}")
    @Produces({"application/json" })
    public final MetaData getLocalMetaData(
             @PathParam("metaDataName") final String metaDataName)
            throws MetaDataNotFoundException {

        MetaDataDao metaDataDao = Resources.getMetaDataDao();
        MetaData metadata = metaDataDao.getByName(metaDataName);

        if (metadata == null) {
            throw new MetaDataNotFoundException(String.format("MetaData set %s could not be found", metaDataName));
        }

        return metadata;
    }

    /**
     * Delete the entire meta data set from the LCM.
     *
     * @param metaDataName The name of the meta data set
     * @return "ok" if successful
     * @throws nl.kpmg.lcm.server.rest.client.MetaDataNotFoundException
     */
    @DELETE
    @Path("{metaDataName}")
    @Produces({"application/json" })
    public final String deleteLocalMetaData(
            @PathParam("metaDataName") final String metaDataName)
            throws MetaDataNotFoundException {

        MetaDataDao metaDataDao = Resources.getMetaDataDao();
        MetaData metadata = metaDataDao.getByName(metaDataName);

        if (metadata == null) {
            throw new MetaDataNotFoundException(String.format("MetaData set %s could not be found", metaDataName));
        }

        metaDataDao.delete(metadata);

        return "ok";
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
    public String postLocalMetaData(
            @PathParam("metaDataName") String metaDataName,
            MetaData metaData) {
        throw new NotImplementedException();
    }

    /**
     * Get meta data of a specific version of a specific meta data set.
     *
     * @param metaDataName The name of the meta data set
     * @param version The version of the meta data set
     * @return The head version of the requested meta data set
     * @throws MetaDataNotFoundException
     */
    @GET
    @Path("{metaDataName}/{version}")
    @Produces({"application/json" })
    public final MetaData getLocalMetaDataByVersion(
            @PathParam("metaDataName") final String metaDataName,
            @PathParam("version") final String version)
            throws MetaDataNotFoundException {

        MetaDataDao metaDataDao = Resources.getMetaDataDao();
        MetaData metadata = metaDataDao.getByNameAndVersion(metaDataName, version);

        if (metadata == null) {
            throw new MetaDataNotFoundException(String.format("MetaData set %s could not be found", metaDataName));
        }

        return metadata;
    }

    /**
     * Delete a version of meta data about a data-set
     *
     * @param metaDataName
     * @param version
     * @return
     */
    @DELETE
    @Path("{metaDataName}/{version}")
    @Produces({"application/json"})
    public String deleteLocalMetaDataByVersion(
            @PathParam("metaDataName") String metaDataName,
            @PathParam("version") String version) {
        throw new NotImplementedException();
    }

    /**
     * Place a fetch request for given data to be stored in a local data store.
     *
     * @param metaDataName
     * @param version
     * @param metaData
     * @return
     */
    @POST
    @Path("{metadata}/{version}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public String postLocalMetaDataByVersion(
            @PathParam("metaDataName") String metaDataName,
            @PathParam("version") String version,
            MetaData metaData) {

        throw new NotImplementedException();
    }
}
