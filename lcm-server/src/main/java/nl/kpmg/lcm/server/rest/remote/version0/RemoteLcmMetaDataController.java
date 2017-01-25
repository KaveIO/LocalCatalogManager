/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
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

import nl.kpmg.lcm.rest.types.FetchEndpointRepresentation;
import nl.kpmg.lcm.rest.types.MetaDataRepresentation;
import nl.kpmg.lcm.rest.types.MetaDatasRepresentation;
import nl.kpmg.lcm.server.data.FetchEndpoint;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.service.FetchEndpointService;
import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteFetchEndpointRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteMetaDataRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.ConcreteMetaDatasRepresentation;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 *
 * @author mhoekstra
 */
@Path("remote/v0/metadata")
public class RemoteLcmMetaDataController {

  @Autowired
  private MetaDataService metaDataService;

  @Autowired
  private StorageService storageService;

  @Autowired
  private FetchEndpointService fetchEndpointService;

  /**
   * Get the head versions of all MetaData.
   *
   * @return The head versions
   */
  @GET
  @Produces({"application/nl.kpmg.lcm.rest.types.MetaDatasRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.REMOTE_USER})
  public final MetaDatasRepresentation getLocalMetaDataOverview(
      @QueryParam("search") String searchString) {

    MetaDatasRepresentation metaDatasRepresentation = new ConcreteMetaDatasRepresentation();
    if (searchString == null || searchString.isEmpty()) {
      List all = metaDataService.findAll();
      metaDatasRepresentation.setRepresentedItems(ConcreteMetaDataRepresentation.class, all);
    } else {
      // TODO Check this!
      MetaData result = metaDataService.findById(searchString);
      List metaDataList = new ArrayList<MetaData>();
      metaDataList.add(result);
      metaDatasRepresentation.setRepresentedItems(ConcreteMetaDataRepresentation.class,
          metaDataList);
    }

    return metaDatasRepresentation;
  }

  /**
   * Get meta data of the head version of a specific meta data set.
   *
   * @param metaDataId The name of the meta data set
   * @return The head version of the requested meta data set
   */
  @GET
  @Path("{meta_data_id}")
  @Produces({"application/nl.kpmg.lcm.rest.types.MetaDataRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.REMOTE_USER})
  public final MetaDataRepresentation getLocalMetaData(
      @PathParam("meta_data_id") final String metaDataId) {

    MetaData metadata = metaDataService.getMetaDataDao().findOne(metaDataId);
    if (metadata == null) {
      throw new NotFoundException(String.format("MetaData set %s could not be found", metaDataId));
    }

    return new ConcreteMetaDataRepresentation(metadata);
  }

  @GET
  @Path("{metadata_id}/fetchUrl")
  @Produces({"application/nl.kpmg.lcm.rest.types.FetchEndpointRepresentation+json"})
  @RolesAllowed({Roles.ADMINISTRATOR, Roles.REMOTE_USER})
  public final FetchEndpointRepresentation generateFetch(
      @PathParam("metadata_id") final String metadata_id) {

    MetaData md = metaDataService.getMetaDataDao().findOne(metadata_id);
    if (md == null) {
      throw new NotFoundException(String.format("Metadata %s not found", metadata_id));
    }

    FetchEndpoint fe = new FetchEndpoint();
    Date now = new Date();
    fe.setCreationDate(new Date());

    // We should read policies to detetermin the time to live. For now we set it one day
    Date later = getTimeToLive(now);

    fe.setTimeToLive(later);
    fe.setMetadataId(md.getId());
    fetchEndpointService.getDao().save(fe);

    return new ConcreteFetchEndpointRepresentation(fe);
  }

  /**
   * This is temporary. We should read policies.
   *
   * @param now
   * @return one day later
   */
  private Date getTimeToLive(Date now) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(now);
    calendar.add(Calendar.DAY_OF_YEAR, 1);
    return calendar.getTime();
  }

}
