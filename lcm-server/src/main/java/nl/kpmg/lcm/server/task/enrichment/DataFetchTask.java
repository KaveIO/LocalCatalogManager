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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.google.gson.stream.JsonReader;

import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendCsvImpl;
import nl.kpmg.lcm.server.backend.Notification;
import nl.kpmg.lcm.server.data.ContentIterator;
import nl.kpmg.lcm.server.data.JsonReaderContentIterator;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.RemoteLcm;
import nl.kpmg.lcm.server.data.service.RemoteLcmService;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.task.EnrichmentTask;
import nl.kpmg.lcm.server.task.TaskException;
import nl.kpmg.lcm.server.task.TaskResult;

import org.glassfish.jersey.client.ClientConfig;
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

  @Autowired
  private StorageService storageService;

  @Autowired
  private RemoteLcmService remoteLcmService;

  // TODO once the Authorization model is implemented this part have to be refactored
  // Now directly is used admin user its password. After the reactoring there
  //should be a user which is used only for remote calls
  private String adminUser;
  private String adminPassword;

  private static final Logger logger = Logger.getLogger(BackendCsvImpl.class.getName());

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
    validation(options, validationNotification);
    if (validationNotification.hasErrors()) {
      throw new TaskException(validationNotification.errorMessage());
    }

    String fetchUrl = getFetchURL(options);

    HttpAuthenticationFeature feature =
        HttpAuthenticationFeature.basicBuilder().nonPreemptive()
            .credentials(adminUser, adminPassword).build();

    ClientConfig clientConfig = new ClientConfig();
    clientConfig.register(feature);

    Client client = ClientBuilder.newClient(clientConfig);
    WebTarget target = client.target(fetchUrl);
    Response response = target.request().get();
    InputStream in = response.readEntity(InputStream.class);

    try {
      JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
      ContentIterator iterator = new JsonReaderContentIterator(reader);
      Backend backend = storageService.getBackend(metadata);
      backend.store(iterator, true);

      metadata.set("dynamic.data.state", "ATTACHED");
      metaDataService.update(metadata.getId(), metadata);
    } catch (Exception ex) {
      logger.log(Level.SEVERE, ex.getMessage());
      return TaskResult.FAILURE;
    }

    return TaskResult.SUCCESS;
  }

  private void validation(Map options, Notification validationNotification) {
    if (options == null) {
      validationNotification.addError("Error! Options could not be null.", null);
      return;
    }

    if (options.get("remoteLcm") == null) {
      validationNotification.addError("Error! Options must contain remote lcm id", null);
    }

    if (options.get("path") == null) {
      validationNotification.addError("Error! Options must contain relative path to the resoruce",
          null);
    }
  }

  private String getFetchURL(Map options) {
    String remoteLcmId = options.get("remoteLcm").toString();
    RemoteLcm remoteLcm = remoteLcmService.findOneById(remoteLcmId);
    String path = options.get("path").toString();
    String fetchUrl = remoteLcm.getUrl() + path;
    return fetchUrl;
  }

}
