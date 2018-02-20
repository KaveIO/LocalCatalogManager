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

import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.LCM_AUTHENTICATION_ORIGIN_HEADER;
import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.LCM_AUTHENTICATION_REMOTE_USER_HEADER;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.client.HttpsClientFactory;
import nl.kpmg.lcm.common.configuration.ClientConfiguration;
import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.DataState;
import nl.kpmg.lcm.common.data.FetchEndpoint;
import nl.kpmg.lcm.common.data.RemoteLcm;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.TaskDescription;
import nl.kpmg.lcm.common.data.TaskType;
import nl.kpmg.lcm.common.data.TransferSettings;
import nl.kpmg.lcm.common.data.TransferValidator;
import nl.kpmg.lcm.common.data.metadata.DataItemsDescriptor;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.common.rest.types.FetchEndpointRepresentation;
import nl.kpmg.lcm.common.rest.types.MetaDataRepresentation;
import nl.kpmg.lcm.server.cron.job.processor.DataFetchExecutor;
import nl.kpmg.lcm.server.rest.client.version0.HttpResponseHandler;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 *
 * @author S. Koulouzis
 */
@Service
public class DataFetchTriggerService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataFetchTriggerService.class
      .getName());

  private static final String SLASH_REPLACEMENT = "_s_";

  @Autowired
  private MetaDataService metaDataService;
  @Autowired
  private StorageService storageService;
  @Autowired
  private RemoteLcmService lcmService;
  @Autowired
  private TaskDescriptionService taskDescriptionService;

  @Autowired 
  private LcmIdService lcmIdService;

  @Autowired
  private ClientConfiguration configuration;

  private static final String METADATA_PATH = "remote/v0/metadata";
  private static final String FETCH_ENDPOINT_CONTROLLER_PATH = "/remote/v0";
  private static final String GENERATE_FETCH_PATH = FETCH_ENDPOINT_CONTROLLER_PATH + "/metadata";
  private static final String FETCH_DATA_PATH = FETCH_ENDPOINT_CONTROLLER_PATH + "/fetch";
  private HttpAuthenticationFeature credentials;

  public void scheduleDataFetchTask(String lcmId, String metadataId, String localStorageId,
      TransferSettings transferSettings, String namespacePath,String username) throws ServerException {
    RemoteLcm lcm = lcmService.findOneById(lcmId);
    if (lcm == null) {
      throw new NotFoundException(String.format("Remote LCM with id: %s is not found", lcmId));
    }

    MetaData existing = metaDataService.findById(metadataId);
    if (existing != null && !transferSettings.isForceOverwrite()) {
      throw new LcmException(
          "This metadata already exists! To enable the transfer enable the overwriting of existing data.");
    }

    MetaDataWrapper metaDataWrapper = getMetadata(metadataId, lcm, username);
    if (metaDataWrapper.isEmpty()) {
      throw new NotFoundException(String.format("Metadata with id: %s is not found", metadataId));
    }
    Storage localStorage = storageService.findById(localStorageId);
    if (localStorage == null) {
      throw new NotFoundException(String.format("Storage with id: %s is not found", localStorageId));
    }
    if (!TransferValidator.validateTransfer(metaDataWrapper.getSourceType(),
        localStorage.getType())) {
      throw new LcmException("Unable to transfer " + metaDataWrapper.getSourceType() + " to "
          + localStorage.getType() + " storage.");
    }

    String executionExpirationTime =
        metaDataWrapper.getExpirationTime().getExecutionExpirationTime();
    if (executionExpirationTime != null) {
      throw new LcmException(
          "Unable to (re)transfer a metadata with already set execution expiration time.");
    }

    updateMetaData(metaDataWrapper, localStorage, namespacePath, lcm.getUniqueId());

    createFetchTask(metaDataWrapper, lcmId, lcm, transferSettings, username);
  }

  private void createFetchTask(MetaDataWrapper metaDataWrapper, String lcmId, RemoteLcm lcm,
      TransferSettings transferSettings, String username) throws ServerException, ClientErrorException {
    TaskDescription dataFetchTaskDescription = new TaskDescription();
    dataFetchTaskDescription.setJob(DataFetchExecutor.class.getName());
    dataFetchTaskDescription.setType(TaskType.FETCH);
    dataFetchTaskDescription.setStatus(TaskDescription.TaskStatus.PENDING);
    dataFetchTaskDescription.setTarget(metaDataWrapper.getId());

    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, 2);
    Date startTime = calendar.getTime();
    dataFetchTaskDescription.setStartTime(startTime);

    Map<String, String> options = new HashMap();
    options.put("remoteLcm", lcmId);
    FetchEndpoint fetchURL = generateFetchURL(metaDataWrapper.getId(), lcm,  username);
    options.put("path", FETCH_DATA_PATH + "/" + fetchURL.getId());
    options.put("username", fetchURL.getUserToConsume());

    dataFetchTaskDescription.setOptions(options);

    dataFetchTaskDescription.setTransferSettings(transferSettings);

    Map<String, String> details = new HashMap();
    String template = "%s://%s:%s";
    String remoteLcmDescription =
        String.format(template, lcm.getProtocol(), lcm.getDomain(), lcm.getPort().toString());

    details.put("remoteLcmURI", remoteLcmDescription);
    details.put("remoteLcmName", lcm.getName());

    details.put("metadataURI", metaDataWrapper.getData().getUri().get(0));
    details.put("metadataName", metaDataWrapper.getName());

    dataFetchTaskDescription.setDetail(details);

    taskDescriptionService.createNew(dataFetchTaskDescription);
  }

  private void updateMetaData(MetaDataWrapper metaDataWrapper, Storage localStorage,
      String namespacePath, String lcmId) throws ServerException, NotFoundException,
      ClientErrorException {

    List<String> uriList = metaDataWrapper.getData().getUri();
    List<String> newUriList = new LinkedList();
    for (String uri : uriList) {
      String metaDataURI = getUpdatedURI(uri, localStorage);
      newUriList.add(metaDataURI);
      metaDataWrapper.getData().setPath(namespacePath);
      Map<String, DataItemsDescriptor> dynamicDataMap =
          metaDataWrapper.getDynamicData().getAllDynamicDataDescriptors();
      for (DataItemsDescriptor descriptor : dynamicDataMap.values()) {
        descriptor.getDetailsDescriptor().setState(DataState.DETACHED);
      }
    }

    for (String key : metaDataWrapper.getDynamicData().getAllDynamicDataDescriptors().keySet()) {

      DataItemsDescriptor dynamicDataDescriptor =
          metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key);

      String uri = dynamicDataDescriptor.getURI();
      String updatedDataItemURI = getUpdatedURI(uri, localStorage);
      dynamicDataDescriptor.setURI(updatedDataItemURI);
    }

    metaDataWrapper.getData().setUri(newUriList);

    metaDataWrapper.getTransferHistory().addSourceLcmId(lcmId);

    String newExecutionTime = metaDataWrapper.getExpirationTime().getTransferExpirationTime();
    if(newExecutionTime != null) {
        metaDataWrapper.getExpirationTime().setExecutionExpirationTime(newExecutionTime);
        metaDataWrapper.getExpirationTime().removeTransferExpirationTime();
    }

    metaDataService.create(metaDataWrapper.getMetaData());
  }

  private String getUpdatedURI(String uri, Storage localStorage) throws ServerException {
    URI originalDataUri = parseDataUri(uri);
    String path = originalDataUri.getPath();
    String originalDataType = originalDataUri.getScheme();
    String newItemName = getUpdatedStorageItemName(originalDataType, localStorage.getType(), path);
    String metaDataURI = localStorage.getType() + "://" + localStorage.getName() + newItemName;
    return metaDataURI;
  }

  private String getUpdatedStorageItemName(String originalDataFormat, String destinationDataFormat,
      String originalItemName) {
    if (originalDataFormat.equals(destinationDataFormat)) {
      return originalItemName;
    }

    String newItemName = originalItemName;
    if (originalDataFormat.equals(DataFormat.CSV) || originalDataFormat.equals(DataFormat.AZURECSV)) {
      if (originalItemName.indexOf(".csv") == originalItemName.length() - 4) {
        newItemName = originalItemName.substring(0, originalItemName.length() - 4);
      } else {
        LOGGER.warn("Illeagal state, 'csv' metadata format without '.csv' file sufix");
      }
    }

    if (originalDataFormat.equals(DataFormat.JSON)) {
      if (originalItemName.indexOf(".json") == originalItemName.length() - 5) {
        newItemName = originalItemName.substring(0, originalItemName.length() - 5);
      } else {
        LOGGER.warn("Illeagal state, 'json' metadata format without '.json' file sufix");
      }
    }

    if (destinationDataFormat.equals(DataFormat.CSV) || destinationDataFormat.equals(DataFormat.AZURECSV)) {
      newItemName = newItemName + ".csv";
    }

    if (destinationDataFormat.equals(DataFormat.JSON)
        || (originalDataFormat.equals(DataFormat.JSON) && TransferValidator
            .isUnstructuredDataFormat(destinationDataFormat))) {
      newItemName = newItemName + ".json";
    }

    // When the source is file based data item i.e csv or json and the destination
    // is DB(Hive, Mongo) and the file path contains folders
    // then transformation must be
    if ((originalDataFormat.equals(DataFormat.CSV) || originalDataFormat.equals(DataFormat.AZURECSV)
            || originalDataFormat.equals(DataFormat.JSON))
        && (destinationDataFormat.equals(DataFormat.HIVE) || destinationDataFormat
            .equals(DataFormat.MONGO))) {

      newItemName = newItemName.substring(1);
      newItemName = newItemName.replaceAll("/", SLASH_REPLACEMENT);
      newItemName = "/" + newItemName;
    }

    return newItemName;
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
  private MetaDataWrapper getMetadata(String metadataId, RemoteLcm lcm, String username) throws ServerException,
      ClientErrorException {
    WebTarget webTarget =
        getWebTarget(lcm).path(METADATA_PATH).path(metadataId).queryParam("update", Boolean.TRUE);
    
    String self = lcmIdService.getLcmIdObject().getLcmId();
    Response response =  webTarget.request()
             .header(LCM_AUTHENTICATION_REMOTE_USER_HEADER, username)
             .header(LCM_AUTHENTICATION_ORIGIN_HEADER, self).get();
    try {
      HttpResponseHandler.handleResponse(response);
    } catch (ClientErrorException ex) {
      throw ex;
    }
    MetaData metaData = response.readEntity(MetaDataRepresentation.class).getItem();

    return new MetaDataWrapper(metaData);
  }

  /**
   * Creates <code>nl.kpmg.lcm.configuration.ClientConfiguration</code> for
   * <code>nl.kpmg.lcm.client.HttpsClientFactory</code>
   *
   * @param targetURI
   * @return the webTarget to contact other LCMs
   * @throws ServerException
   */
  private WebTarget getWebTarget(RemoteLcm lcm) throws ServerException {
    if (credentials == null) {
      credentials =
          HttpAuthenticationFeature.basicBuilder()
                  .credentials(lcm.getApplicationId(), lcm.getApplicationKey()).build();
    }
    HttpsClientFactory clientFactory = new HttpsClientFactory(configuration, credentials);
    configuration.setTargetHost(lcm.getDomain());
    configuration.setTargetPort(lcm.getPort().toString());
    return clientFactory.createWebTarget(buildRemoteUrl(lcm));
  }

  /**
   * Contact remote lcm to generate <code>FetchEndpoint</code> object
   *
   * @param metadataId
   * @param remoteLcmURL
   * @return the FetchEndpoint
   */
  private FetchEndpoint generateFetchURL(String metadataId, RemoteLcm lcm,  String username) throws ServerException,
      ClientErrorException {
    WebTarget webTarget = getWebTarget(lcm);
    String self =  lcmIdService.getLcmIdObject().getLcmId();
    Response response =
        webTarget.path(GENERATE_FETCH_PATH).path(metadataId).path("fetchUrl").request()
             .header(LCM_AUTHENTICATION_REMOTE_USER_HEADER, username)
             .header(LCM_AUTHENTICATION_ORIGIN_HEADER, self).get();
    try {
      HttpResponseHandler.handleResponse(response);
    } catch (ClientErrorException ex) {
      throw ex;
    }
    return response.readEntity(FetchEndpointRepresentation.class).getItem();
  }

  private String buildRemoteUrl(RemoteLcm lcm) {
    String url = String.format("%s://%s", lcm.getProtocol(), lcm.getDomain());
    if (lcm.getPort() != null) {
      url += ":" + lcm.getPort();
    }
    return url;
  }

  private URI parseDataUri(String uri) throws ServerException {

    try {
      return new URI(uri);
    } catch (URISyntaxException ex) {
      throw new ServerException(String.format("Failure while trying to parse URI '%s'", uri), ex);
    }
  }
}
