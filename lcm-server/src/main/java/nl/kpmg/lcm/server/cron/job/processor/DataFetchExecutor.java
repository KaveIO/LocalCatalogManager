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

package nl.kpmg.lcm.server.cron.job.processor;

import nl.kpmg.lcm.server.cron.job.AbstractDataProcessor;

import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.LCM_AUTHENTICATION_ORIGIN_HEADER;
import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.LCM_AUTHENTICATION_REMOTE_USER_HEADER;

import com.google.gson.stream.JsonReader;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.client.HttpsClientFactory;
import nl.kpmg.lcm.common.configuration.ClientConfiguration;
import nl.kpmg.lcm.common.data.ContentIterator;
import nl.kpmg.lcm.common.data.Data;
import nl.kpmg.lcm.common.data.EnrichmentProperties;
import nl.kpmg.lcm.common.data.IterativeData;
import nl.kpmg.lcm.common.data.ProgressIndication;
import nl.kpmg.lcm.common.data.RemoteLcm;
import nl.kpmg.lcm.common.data.StreamingData;
import nl.kpmg.lcm.common.data.TaskDescription;
import nl.kpmg.lcm.common.data.TransferSettings;
import nl.kpmg.lcm.common.data.metadata.DataItemsDescriptor;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.common.validation.Notification;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.data.JsonReaderContentIterator;
import nl.kpmg.lcm.server.data.ProgressIndicationFactory;
import nl.kpmg.lcm.server.data.service.LcmIdService;
import nl.kpmg.lcm.server.data.service.RemoteLcmService;
import nl.kpmg.lcm.server.cron.exception.CronJobExecutionException;
import nl.kpmg.lcm.server.cron.TaskResult;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.ws.rs.core.Response;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class DataFetchExecutor extends AbstractDataProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataFetchExecutor.class.getName());

  @Autowired
  private RemoteLcmService remoteLcmService;

  @Autowired
  private LcmIdService lcmIdService;

  @Autowired
  private ClientConfiguration configuration;

  private String applicationId;
  private String applicationKey;
  private String username;

  @Override
  protected TaskResult execute(MetaDataWrapper metadata, Map options) throws CronJobExecutionException {

    Notification validationNotification = new Notification();
    validation(options, metadata, validationNotification);
    if (validationNotification.hasErrors()) {
      throw new CronJobExecutionException(validationNotification.errorMessage());
    }

    initConfiguration(options);
    TransferSettings transferSettings = getTransferSettings();
      if (metadata.getDynamicData() == null
              || metadata.getDynamicData().getAllDynamicDataDescriptors() == null
              || metadata.getDynamicData().getAllDynamicDataDescriptors().isEmpty()) {
          taskDescriptionService.updateProgress(taskId, new ProgressIndication(
                  "No dynamic data items detected!"));
          return TaskResult.FAILURE;
      }

    Map<String, DataItemsDescriptor> dynamicDataMap = metadata.getDynamicData().getAllDynamicDataDescriptors();
    for(String key : dynamicDataMap.keySet()) {
        String fetchUrl = buildFetchURL(options, key);
        InputStream in = openInputStream(fetchUrl);
        if (!writeData(in, metadata, key, transferSettings)) {
          taskDescriptionService.updateProgress(taskId, new ProgressIndication(
              "Task excecution failed!"));
          return TaskResult.FAILURE;
        }
    }

    metaDataService.enrichMetadata(metadata, EnrichmentProperties.createDefaultEnrichmentProperties());
    return TaskResult.SUCCESS;
  }

  private TransferSettings getTransferSettings() {
    TaskDescription taskDescription = taskDescriptionService.findOne(taskId);
    TransferSettings settings = taskDescription.getTransferSettings();
    if (settings == null) {
      LOGGER.warn("No trasformations settings are found and default ones willbe used!");
      return new TransferSettings();
    }

    return settings;
  }

  private InputStream openInputStream(String fetchUrl) throws CronJobExecutionException {

    HttpAuthenticationFeature credentials =
        HttpAuthenticationFeature.basicBuilder().credentials(applicationId, applicationKey).build();

    HttpsClientFactory clientFactory = new HttpsClientFactory(configuration, credentials);

    Response response = null;

    try {
      String self = lcmIdService.getLcmIdObject().getLcmId();
      response = clientFactory.createWebTarget(fetchUrl).request()
              .header(LCM_AUTHENTICATION_REMOTE_USER_HEADER, username)
              .header(LCM_AUTHENTICATION_ORIGIN_HEADER, self).get();
    } catch (ServerException ex) {
      throw new CronJobExecutionException(ex);
    }
    if (response.getStatus() != Response.Status.OK.getStatusCode()) {
      String responseMessage = response.readEntity(String.class);
      responseMessage = responseMessage.substring(0, Math.min(responseMessage.length(), 300));
      throw new CronJobExecutionException(responseMessage);
    }

    return response.readEntity(InputStream.class);
  }

  private boolean writeData(InputStream in, MetaDataWrapper metaDataWrapper, String key,
      TransferSettings transferSettings) {
    try {
      Backend backend = storageService.getBackend(metaDataWrapper);
      backend.setProgressIndicationFactory(new ProgressIndicationFactory(taskDescriptionService,
          taskId, 10000));
      Data data;
      String dataType = metaDataWrapper.getData().getDataType();
      if (StreamingData.getStreamingDataTypes().contains(dataType)) {
        data = new StreamingData(in);
      } else {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        ContentIterator iterator = new JsonReaderContentIterator(reader);
        data = new IterativeData(iterator);
      }

      backend.store(data, key,transferSettings);
    } catch (Exception ex) {
      LOGGER.error(ex.getMessage());

      return false;
    }
    return true;
  }

  private void validation(Map options, MetaDataWrapper metaDataWrapper,
      Notification validationNotification) {
    if (metaDataWrapper.isEmpty()) {
      validationNotification.addError("Error! MetaData parameter could not be null.", null);
    }

    if (options == null) {
      validationNotification.addError("Error! Options parameter could not be null.", null);
      return;
    }

    if (options.get("remoteLcm") == null) {
      validationNotification.addError("Error! Options parameter must contain remote lcm id", null);
    }

    if (options.get("path") == null) {
      validationNotification.addError(
          "Error! Options parameter must contain relative path to the resource", null);
    }
  }

  private String buildFetchURL(Map options, String key) {
    String remoteLcmId = options.get("remoteLcm").toString();
    RemoteLcm remoteLcm = remoteLcmService.findOneById(remoteLcmId);
    String path = options.get("path").toString();
    String fetchUrl = buildRemoteUrl(remoteLcm) + path + "?data_key=" + key;

    return fetchUrl;
  }

  private String buildRemoteUrl(RemoteLcm lcm) {
    String url = String.format("%s://%s", lcm.getProtocol(), lcm.getDomain());
    if (lcm.getPort() != null) {
      url += ":" + lcm.getPort();
    }
    return url;
  }

  private void initConfiguration(Map options) {
    String remoteLcmId = options.get("remoteLcm").toString();
    RemoteLcm remoteLcm = remoteLcmService.findOneById(remoteLcmId);
    applicationId = remoteLcm.getApplicationId();
    applicationKey = remoteLcm.getApplicationKey();
    username = (String)options.get("username");
    configuration.setTargetHost(remoteLcm.getDomain());
    configuration.setTargetPort(remoteLcm.getPort().toString());
  }
}
