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

package nl.kpmg.lcm.server.data.service;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import nl.kpmg.lcm.client.HttpsClientFactory;
import nl.kpmg.lcm.configuration.ClientConfiguration;
import nl.kpmg.lcm.rest.types.FetchEndpointRepresentation;
import nl.kpmg.lcm.rest.types.MetaDataRepresentation;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.FetchEndpoint;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.RemoteLcm;
import nl.kpmg.lcm.server.data.TaskDescription;
import nl.kpmg.lcm.server.data.dao.RemoteLcmDao;
import nl.kpmg.lcm.server.rest.client.version0.HttpResponseHandler;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author S. Koulouzis
 */
@Service
public class DataFetchTriggerService {

  @Autowired
  private MetaDataService metaDataService;
  @Autowired
  private RemoteLcmService lcmService;
  @Autowired
  private TaskDescriptionService taskDescriptionService;
  @Autowired
  @Value("${lcm.server.adminUser}")
  private String adminUser;
  @Autowired
  @Value("${lcm.server.adminPassword}")
  private String adminPassword;
  @Autowired
  private ClientConfiguration configuration;

  private static final String METADATA_PATH = "client/v0/local";
  private static final String FETCH_ENDPOINT_CONTROLLER_PATH = "/remote/v0";
  private static final String GENERATE_FETCH_PATH = FETCH_ENDPOINT_CONTROLLER_PATH + "/metadata";
  private static final String FETCH_DATA_PATH = FETCH_ENDPOINT_CONTROLLER_PATH + "/fetch";
  private HttpAuthenticationFeature credentials;

  public void scheduleDataFetchTask(String lcmId, String metadataId) throws ServerException {
    RemoteLcmDao dao = lcmService.getDao();
    RemoteLcm lcm = dao.findOneById(lcmId);
    if (lcm == null) {
      throw new NotFoundException(String.format("LCM %s not found", lcmId));
    }
    String remoteLcmURL = lcm.getUrl();

    FetchEndpoint fetchURL;
    MetaData md = getMetadata(metadataId, remoteLcmURL);
    if (md == null) {
      throw new NotFoundException(String.format("Metadata %s not found", metadataId));
    }
    metaDataService.getMetaDataDao().save(md);
    fetchURL = generateFetchURL(metadataId, remoteLcmURL);
    TaskDescription dataFetchTaskDescription = new TaskDescription();
    dataFetchTaskDescription.setJob(this.getClass().getName());
    Map<String, String> options = new HashMap();
    options.put("remoteLcm", lcmId);
    options.put("path", FETCH_DATA_PATH + "/" + fetchURL.getId());
    dataFetchTaskDescription.setOptions(options);
    taskDescriptionService.getTaskDescriptionDao().save(dataFetchTaskDescription);
  }

  /**
   * Gets metadata from remote LCM.
   *
   * @param metadataId
   * @param remoteLcmUrl
   * @return
   * @throws ServerException
   * @throws ClientErrorException
   */
  private MetaData getMetadata(String metadataId, String remoteLcmUrl) throws
          ServerException, ClientErrorException {
    WebTarget webTarget = getWebTarget(remoteLcmUrl).path(METADATA_PATH).path(metadataId);
    Invocation.Builder req = webTarget.request();
    Response response = req.get();
    try {
      HttpResponseHandler.handleResponse(response);
    } catch (ClientErrorException ex) {
      throw ex;
    }
    return response
            .readEntity(MetaDataRepresentation.class).getItem();
  }

  /**
   * Creates <code>nl.kpmg.lcm.configuration.ClientConfiguration</code> for
   * <code>nl.kpmg.lcm.client.HttpsClientFactory</code>
   *
   * @param targetURI
   * @return the webTarget to contact other LCMs
   * @throws ServerException
   */
  private WebTarget getWebTarget(String targetURI) throws ServerException {
    if (credentials == null) {
      credentials
              = HttpAuthenticationFeature.basicBuilder().nonPreemptive()
              .credentials(adminUser, adminPassword).build();
    }
    HttpsClientFactory clientFactory = new HttpsClientFactory(configuration, credentials);
    return clientFactory.createWebTarget(targetURI);
  }

  /**
   * Contact remote lcm to generate <code>FetchEndpoint</code> object
   *
   * @param metadataId
   * @param remoteLcmURL
   * @return the FetchEndpoint
   */
  private FetchEndpoint generateFetchURL(String metadataId, String remoteLcmURL)
          throws ServerException, ClientErrorException {
    WebTarget webTarget = getWebTarget(remoteLcmURL);
    Response response = webTarget.path(GENERATE_FETCH_PATH)
            .path(metadataId)
            .path("fetchUrl")
            .request()
            .get();
    try {
      HttpResponseHandler.handleResponse(response);
    } catch (ClientErrorException ex) {
      throw ex;
    }
    return response
            .readEntity(FetchEndpointRepresentation.class).getItem();
  }
}
