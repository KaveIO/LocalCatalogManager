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

package nl.kpmg.lcm.ui.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WrappedSession;

import nl.kpmg.lcm.client.ClientException;
import nl.kpmg.lcm.client.HttpsClientFactory;
import nl.kpmg.lcm.configuration.ClientConfiguration;
import nl.kpmg.lcm.rest.types.AbstractRepresentation;
import nl.kpmg.lcm.rest.types.MetaDatasRepresentation;
import nl.kpmg.lcm.rest.types.StoragesRepresentation;
import nl.kpmg.lcm.rest.types.TaskDescriptionsRepresentation;
import nl.kpmg.lcm.rest.types.TaskScheduleRepresentation;
import nl.kpmg.lcm.rest.types.UserGroupsRepresentation;
import nl.kpmg.lcm.rest.types.UsersRepresentation;
import nl.kpmg.lcm.server.ServerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

/**
 *
 * @author mhoekstra
 */
@Component
public class RestClientService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestClientService.class.getName());

  private final String uri;
  private final String unsafeUri;

  @Autowired
  private HttpsClientFactory clientFactory;

  private boolean secure = true;

  @Autowired
  public RestClientService(ClientConfiguration configuration) {
    uri = String.format("https://%s:%s/", configuration.getTargetHost(),
        configuration.getTargetPort());
    unsafeUri = String.format("http://%s:%s/", configuration.getTargetHost(),
        configuration.getUnsafeTargetPort());
  }

  private void saveLoginToken(String loginToken) {
    VaadinService.getCurrentRequest().getWrappedSession().setAttribute("loginToken", loginToken);
  }

  private void saveLoginUser(String loginUser) {
    VaadinService.getCurrentRequest().getWrappedSession().setAttribute("loginUser", loginUser);
  }

  private String retrieveLoginToken() {
    WrappedSession wrappedSession = VaadinService.getCurrentRequest().getWrappedSession();
    return (String) wrappedSession.getAttribute("loginToken");
  }

  private String retrieveLoginUser() {
    WrappedSession wrappedSession = VaadinService.getCurrentRequest().getWrappedSession();
    return (String) wrappedSession.getAttribute("loginUser");
  }

  private Response post(String uri, String path, Entity<String> payload) throws ServerException {
    return clientFactory.createWebTarget(uri).path(path).request().post(payload);
  }

  public void authenticate(String username, String password)
      throws AuthenticationException, ServerException {
    String path = "client/login";
    Entity<String> payload = Entity.entity(
        String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password),
        "application/nl.kpmg.lcm.server.rest.client.types.LoginRequest+json");
    Response post;
    try {
      post = post(uri, path, payload);
    } catch (ServerException | ProcessingException e) {
      LOGGER.warn(
          "Server error in LCM target server HTTPS REST invocation, trying HTTP...", e);
      post = post(unsafeUri, path, payload);
      secure = false;
    }
    if (post.getStatus() != 200) {
      throw new AuthenticationException("Login failed");
    }

    saveLoginUser(username);
    saveLoginToken(post.readEntity(String.class));
  }

  public boolean isAuthenticated() {
    String retrieveLoginToken = retrieveLoginToken();
    String retrieveLoginUser = retrieveLoginUser();
    return retrieveLoginToken != null && !retrieveLoginToken.isEmpty() && retrieveLoginUser != null
        && !retrieveLoginUser.isEmpty();
  }

  public Invocation.Builder getClient(String path) throws AuthenticationException, ServerException {
    if (!isAuthenticated()) {
      throw new AuthenticationException("Not logged in");
    }

    String uri;
    if (secure) {
      uri = this.uri;
    } else {
      uri = this.unsafeUri;
    }

    return clientFactory.createWebTarget(uri).path(path).request()
        .header("LCM-Authentication-User", retrieveLoginUser())
        .header("LCM-Authentication-Token", retrieveLoginToken());
  }

  /**
   * Helper method for requesting data from the LCM-Server.
   *
   * Making a correct and complete request to the LCM-Server is a bit cumbersome. This method helps
   * in making the correct status checks an logging around this.
   *
   * @param <T> The correct derivative of a AbstractDatasRepresentation to which that data gets
   *        loaded
   * @param path to execute the get against
   * @param type type of concrete AbstractDatasRepresentation to load the data in
   * @return the data from given path deserialized in object of type T
   *
   * @throws AuthenticationException when not (or incorrectly) logged in
   * @throws ServerException when the server could not be reached
   * @throws ClientException when the request fails
   */
  public <T extends AbstractRepresentation> T getDatasRepresentation(String path, Class<T> type)
      throws AuthenticationException, ServerException, ClientException {

    LOGGER.info(String.format("Executing get on LCM-Server on path: %s", path));
    Response response = getClient(path).get();
    if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
      T datasRepresentation = response.readEntity(type);
      return datasRepresentation;
    } else {
      LOGGER.warn(String.format("Call to LCM-Server failed with: %d - %s",
          response.getStatus(), response.getStatusInfo().getReasonPhrase()));
      throw new ClientException("Call to LCM-Server failed", response);
    }
  }

  public MetaDatasRepresentation getLocalMetadata()
      throws AuthenticationException, ServerException, ClientException {
    return getDatasRepresentation("client/v0/local", MetaDatasRepresentation.class);
  }

  public StoragesRepresentation getStorage()
      throws AuthenticationException, ServerException, ClientException {
    return getDatasRepresentation("client/v0/storage", StoragesRepresentation.class);
  }


  public TaskDescriptionsRepresentation getTasks()
      throws AuthenticationException, ServerException, ClientException {
    return getDatasRepresentation("client/v0/tasks", TaskDescriptionsRepresentation.class);
  }

  public TaskScheduleRepresentation getTaskSchedule()
      throws AuthenticationException, ServerException, ClientException {
    return getDatasRepresentation("client/v0/taskschedule", TaskScheduleRepresentation.class);
  }

  public UsersRepresentation getUsers()
      throws AuthenticationException, ServerException, ClientException {
    return getDatasRepresentation("client/v0/users", UsersRepresentation.class);
  }

  public UserGroupsRepresentation getUserGroups()
      throws AuthenticationException, ServerException, ClientException {
    return getDatasRepresentation("client/v0/userGroups", UserGroupsRepresentation.class);
  }

  public void createStorage(String storage)
      throws ServerException, DataCreationException, AuthenticationException, JsonProcessingException {
      Entity<String> payload =
        Entity.entity(storage, "application/nl.kpmg.lcm.server.data.Storage+json");

    Invocation.Builder client = getClient("client/v0/storage");
    Response post = client.post(payload);

    Response.StatusType statusInfo = post.getStatusInfo();
    if (statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL) {
      throw new DataCreationException(
          String.format("%s - %s", statusInfo.getStatusCode(), statusInfo.getReasonPhrase()));
    }
  }

  public void updateStorage(String storage)
      throws ServerException, DataCreationException, AuthenticationException, JsonProcessingException {
      Entity<String> payload =
        Entity.entity(storage, "application/nl.kpmg.lcm.server.data.Storage+json");

    Invocation.Builder client = getClient("client/v0/storage");
    Response put = client.put(payload);

    Response.StatusType statusInfo = put.getStatusInfo();
    if (statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL) {
      throw new DataCreationException(
          String.format("%s - %s", statusInfo.getStatusCode(), statusInfo.getReasonPhrase()));
    }
  }

  public void deleteStorage(String storageId) throws AuthenticationException, ServerException,
      ClientException, DataCreationException {

    Invocation.Builder client = getClient(String.format("client/v0/storage/%s", storageId));
    Response delete = client.delete();

    Response.StatusType statusInfo = delete.getStatusInfo();
    if (statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL) {
      throw new DataCreationException(String.format("%s - %s", statusInfo.getStatusCode(),
          statusInfo.getReasonPhrase()));
    }
  }

  public void postMetadata(String metadata)
      throws ServerException, DataCreationException, AuthenticationException {
    Entity<String> payload =
        Entity.entity(metadata, "application/nl.kpmg.lcm.server.data.MetaData+json");

    Invocation.Builder client = getClient("client/v0/local");
    Response post = client.post(payload);

    Response.StatusType statusInfo = post.getStatusInfo();
    if (statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL) {
      throw new DataCreationException(
          String.format("%s - %s", statusInfo.getStatusCode(), statusInfo.getReasonPhrase()));
    }
  }

  public void putMetadata(String id, String metadata)
      throws AuthenticationException, ServerException, DataCreationException {
    Entity<String> payload =
        Entity.entity(metadata, "application/nl.kpmg.lcm.server.data.MetaData+json");

    Invocation.Builder client = getClient(String.format("client/v0/local/%s", id));
    Response put = client.put(payload);

    Response.StatusType statusInfo = put.getStatusInfo();
    if (statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL) {
      throw new DataCreationException(
          String.format("%s - %s", statusInfo.getStatusCode(), statusInfo.getReasonPhrase()));
    }
  }

  public void deleteMetadata(String id)
      throws DataCreationException, AuthenticationException, ServerException {
    Invocation.Builder client = getClient(String.format("client/v0/local/%s", id));
    Response delete = client.delete();

    Response.StatusType statusInfo = delete.getStatusInfo();
    if (statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL) {
      throw new DataCreationException(
          String.format("%s - %s", statusInfo.getStatusCode(), statusInfo.getReasonPhrase()));
    }
  }
}
