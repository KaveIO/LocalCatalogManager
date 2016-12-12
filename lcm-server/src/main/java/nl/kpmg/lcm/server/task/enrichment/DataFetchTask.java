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

package nl.kpmg.lcm.server.task.enrichment;

import javax.ws.rs.core.Response;

import com.google.gson.stream.JsonReader;

import nl.kpmg.lcm.client.HttpsClientFactory;
import nl.kpmg.lcm.configuration.ClientConfiguration;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendCsvImpl;
import nl.kpmg.lcm.server.backend.DataTransformationSettings;
import nl.kpmg.lcm.server.data.ContentIterator;
import nl.kpmg.lcm.server.data.JsonReaderContentIterator;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.RemoteLcm;
import nl.kpmg.lcm.server.data.service.RemoteLcmService;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.task.EnrichmentTask;
import nl.kpmg.lcm.server.task.TaskException;
import nl.kpmg.lcm.server.task.TaskResult;
import nl.kpmg.lcm.validation.Notification;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class DataFetchTask extends EnrichmentTask {

  private static final Logger logger = Logger.getLogger(BackendCsvImpl.class.getName());

  @Autowired
  private StorageService storageService;

  @Autowired
  private RemoteLcmService remoteLcmService;

  @Autowired
  private ClientConfiguration configuration;

  // TODO once the Authorization model is implemented this part may be refactored
  // Now directly is used admin user and its password. After the refactoring there
  // could be a user which is used only for remote calls
  private String adminUser;
  private String adminPassword;

  @Value("${lcm.server.adminUser}")
  public final void setAdminUser(final String adminUser) {
    this.adminUser = adminUser;
  }

  @Value("${lcm.server.adminPassword}")
  public final void setAdminPassword(final String adminPassword) {
    this.adminPassword = adminPassword;
  }

  @Override
  protected TaskResult execute(MetaData metadata, Map options) throws TaskException {

    Notification validationNotification = new Notification();
    validation(options, metadata, validationNotification);
    if (validationNotification.hasErrors()) {
      throw new TaskException(validationNotification.errorMessage());
    }

    initConfiguration(options);
    String fetchUrl = buildFetchURL(options);

    InputStream in = openInputStream(fetchUrl);

    if (!writeData(in, metadata)) {
      return TaskResult.FAILURE;
    }

    return TaskResult.SUCCESS;
  }

  private InputStream openInputStream(String fetchUrl) throws TaskException {
    HttpAuthenticationFeature credentials = HttpAuthenticationFeature.basicBuilder()
        .credentials(adminUser, adminPassword).build();

    HttpsClientFactory clientFactory = new HttpsClientFactory(configuration, credentials);

    Response response =  null;
    try {
      response = clientFactory.createWebTarget(fetchUrl).request().get();
    } catch (ServerException ex) {
      logger.log(Level.SEVERE, null, ex);
      throw new TaskException(ex);
    }

    return response.readEntity(InputStream.class);
  }

  private boolean writeData(InputStream in, MetaData metadata) {
    try {
      JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
      ContentIterator iterator = new JsonReaderContentIterator(reader);
      Backend backend = storageService.getBackend(metadata);
      backend.store(iterator, new DataTransformationSettings(), true);

      metadata.set("dynamic.data.state", "ATTACHED");
      metaDataService.update(metadata.getId(), metadata);
    } catch (Exception ex) {
      logger.log(Level.SEVERE, ex.getMessage());

      return false;
    }
    return true;
  }

  private void validation(Map options, MetaData metadata, Notification validationNotification) {
    if (metadata == null) {
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
      validationNotification
          .addError("Error! Options parameter must contain relative path to the resoruce", null);
    }
  }

  private String buildFetchURL(Map options) {
    String remoteLcmId = options.get("remoteLcm").toString();
    RemoteLcm remoteLcm = remoteLcmService.findOneById(remoteLcmId);
    String path = options.get("path").toString();
    String fetchUrl = buildRemoteUrl(remoteLcm) + path;

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
    configuration.setTargetHost(remoteLcm.getDomain());
    configuration.setTargetPort(remoteLcm.getPort().toString());
  }
}
