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

import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.LCM_AUTHENTICATION_ORIGIN_HEADER;
import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.LCM_AUTHENTICATION_TOKEN_HEADER;
import static nl.kpmg.lcm.common.rest.authentication.AuthorizationConstants.LCM_AUTHENTICATION_USER_HEADER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WrappedSession;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.client.HttpsClientFactory;
import nl.kpmg.lcm.common.configuration.ClientConfiguration;
import nl.kpmg.lcm.common.data.TaskType;
import nl.kpmg.lcm.common.data.TestResult;
import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.rest.types.AbstractRepresentation;
import nl.kpmg.lcm.common.rest.types.AuthorizedLcmsRepresentation;
import nl.kpmg.lcm.common.rest.types.LcmIdRepresentation;
import nl.kpmg.lcm.common.rest.types.MetaDatasRepresentation;
import nl.kpmg.lcm.common.rest.types.RemoteLcmsRepresentation;
import nl.kpmg.lcm.common.rest.types.StoragesRepresentation;
import nl.kpmg.lcm.common.rest.types.TaskDescriptionsRepresentation;
import nl.kpmg.lcm.common.rest.types.TaskScheduleRepresentation;
import nl.kpmg.lcm.common.rest.types.UserGroupsRepresentation;
import nl.kpmg.lcm.common.rest.types.UsersRepresentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
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

  private void clearLoginToken() {
    VaadinService.getCurrentRequest().getWrappedSession().removeAttribute("loginToken");
  }

  private void clearLoginUser() {
    VaadinService.getCurrentRequest().getWrappedSession().removeAttribute("loginUser");
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
    return clientFactory.createWebTarget(uri).path(path).request()
            .header(LCM_AUTHENTICATION_ORIGIN_HEADER, User.LOCAL_ORIGIN).post(payload);
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

  public void logout() throws AuthenticationException {
    String path = "client/logout";

    Entity<String> payload =
        Entity.entity("{}", "application/nl.kpmg.lcm.server.rest.client.types.LogoutRequest+json");

    Response post = null;
    try {
      post = getClient(path).post(payload);
    } catch (ServerException | ProcessingException e) {
      LOGGER.warn("Server error in LCM target server HTTPS REST invocation, trying HTTP...", e);
      secure = false;
    }
    if (post == null || post.getStatus() != 200) {
      throw new AuthenticationException("Logout failed");
    }

    clearLoginUser();
    clearLoginToken();
  }

  public boolean isAuthenticated() {
    String retrieveLoginToken = retrieveLoginToken();
    String retrieveLoginUser = retrieveLoginUser();
    return retrieveLoginToken != null && !retrieveLoginToken.isEmpty() && retrieveLoginUser != null
        && !retrieveLoginUser.isEmpty();
  }

  public Invocation.Builder getClient(String path) throws AuthenticationException, ServerException {
      return getClient(path, new HashMap());
  }

  public Invocation.Builder getClient(String path, Map<String, String> parameters) throws AuthenticationException, ServerException {
    if (!isAuthenticated()) {
      throw new AuthenticationException("Not logged in");
    }

    String uri;
    if (secure) {
      uri = this.uri;
    } else {
      uri = this.unsafeUri;
    }

    WebTarget target = clientFactory.createWebTarget(uri).path(path);
    for(String key : parameters.keySet()){
        String value =  parameters.get(key);
        target = target.queryParam(key, value);
    }

    return target.request()
        .header(LCM_AUTHENTICATION_USER_HEADER, retrieveLoginUser())
        .header(LCM_AUTHENTICATION_TOKEN_HEADER, retrieveLoginToken());
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
   * @throws LcmBadRequestException when the request fails
   */
  public <T extends AbstractRepresentation> T getDatasRepresentation(String path, Class<T> type,
          Map<String, String> parameters)
      throws AuthenticationException, ServerException, LcmBadRequestException {

    LOGGER.info(String.format("Executing get on LCM-Server on path: %s", path));

    Response response = getClient(path, parameters).get();
    
    checkForErrors(response);
    
    T datasRepresentation = response.readEntity(type);
    return datasRepresentation;
  }

  public <T extends AbstractRepresentation> T getDatasRepresentation(String path, Class<T> type)
      throws AuthenticationException, ServerException, LcmBadRequestException {

   return getDatasRepresentation(path, type, new HashMap<String,String>());
  }

  public MetaDatasRepresentation getLocalMetadata()
      throws AuthenticationException, ServerException, LcmBadRequestException {
    return getDatasRepresentation("client/v0/local", MetaDatasRepresentation.class);
  }

   public MetaDatasRepresentation getRemoteMetadata(String remoteLcmId)
      throws AuthenticationException, ServerException, LcmBadRequestException {
    return getDatasRepresentation("client/v0/remote/"+remoteLcmId+"/search" , MetaDatasRepresentation.class);
  }

  public StoragesRepresentation getStorage()
      throws AuthenticationException, ServerException, LcmBadRequestException {
    return getDatasRepresentation("client/v0/storage", StoragesRepresentation.class);
  }

  public AuthorizedLcmsRepresentation getAuthorizedLcms()
      throws AuthenticationException, ServerException, LcmBadRequestException {
    return getDatasRepresentation("client/v0/authorizedlcm", AuthorizedLcmsRepresentation.class);
  }

  public RemoteLcmsRepresentation getRemoteLcm() throws AuthenticationException, ServerException,
      LcmBadRequestException {
    return getDatasRepresentation("client/v0/remoteLcm", RemoteLcmsRepresentation.class);
  }

  public TestResult testRemoteLcm(String remoteLcmId) {
    String path = "client/v0/remoteLcm/status/" + remoteLcmId;
    LOGGER.info(String.format("Executing test remote LCM on LCM-Server on path: %s", path));
    try {
      Response response = getClient(path).get();
      if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
        TestResult result = response.readEntity(TestResult.class);
        return result;
      } else {
        String message =
            String.format("Test to LCM-Server failed with: %d - %s", response.getStatus(), response
                .getStatusInfo().getReasonPhrase());
        LOGGER.info(message);
        return new TestResult(message, TestResult.TestCode.INACCESSIBLE);
      }
    } catch (AuthenticationException ex) {
      String message =
          String.format("Test to LCM-Server failed with authentication exception: %s ",
              ex.getMessage());
      LOGGER.info(message);
      return new TestResult(message, TestResult.TestCode.INACCESSIBLE);
    } catch (ServerException ex) {
      String message =
          String.format("Test to LCM-Server failed with server exception: %s ", ex.getMessage());
      LOGGER.info(message);
      return new TestResult(message, TestResult.TestCode.INACCESSIBLE);
    }
  }

  public String exportUsersToRemoteLcm(String remoteLcmId) {
    String path = "client/v0/remoteLcm/" + remoteLcmId + "/export-users";
    LOGGER.info(String.format("Executing export of users to remote LCM. Path: %s", path));
    try {
      Response response = getClient(path).post(null);
      if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
        return "OK";
      } else if (response.getStatus() == 400) {
        String message = "The remote LCM did not allow importing of users from the local LCM.";
        LOGGER.info(message);
        return message;
      } else {
        String message =
            String.format("The export of users to remote LCM failed with: %d - %s",
                response.getStatus(), response.getStatusInfo().getReasonPhrase());
        LOGGER.info(message);
        return message;
      }
    } catch (AuthenticationException | ServerException ex) {
      String message =
          String.format("The export of users to remote LCM failed with server exception: %s ",
              ex.getMessage());
      LOGGER.info(message);
      return message;
    }
  }

  public TaskDescriptionsRepresentation getFetchTasks() throws AuthenticationException,
      ServerException, LcmBadRequestException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("type", TaskType.FETCH.name());
    return getDatasRepresentation("client/v0/tasks", TaskDescriptionsRepresentation.class,
        parameters);
  }

  public TaskDescriptionsRepresentation getLastTasks(Integer maximumItems)
      throws AuthenticationException, ServerException, LcmBadRequestException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("limit", String.valueOf(maximumItems));
    return getDatasRepresentation("client/v0/tasks", TaskDescriptionsRepresentation.class, parameters);
  }

  public TaskScheduleRepresentation getTaskSchedule()
      throws AuthenticationException, ServerException, LcmBadRequestException {
    return getDatasRepresentation("client/v0/taskschedule", TaskScheduleRepresentation.class);
  }

  public UsersRepresentation getUsers()
      throws AuthenticationException, ServerException, LcmBadRequestException {
    return getDatasRepresentation("client/v0/users", UsersRepresentation.class);
  }

  public UserGroupsRepresentation getUserGroups() throws AuthenticationException, ServerException,
      LcmBadRequestException {
    return getDatasRepresentation("client/v0/userGroups", UserGroupsRepresentation.class);
  }

  public void createUserGroup(String userGroup) throws ServerException, LcmBadRequestException,
      AuthenticationException, JsonProcessingException {
    Entity<String> payload =
        Entity.entity(userGroup, "application/nl.kpmg.lcm.server.data.UserGroup+json");

    Invocation.Builder client = getClient("client/v0/userGroups");
    Response post = client.post(payload);

    checkForErrors(post);
  }

  private void checkForErrors(Response response) throws LcmBadRequestException {
    Response.StatusType statusInfo = response.getStatusInfo();
    if (statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL) {
      String message = response.readEntity(String.class);
      throw new LcmBadRequestException(String.format("%s - %s", statusInfo.getStatusCode(), message));
    }
  }

  public void updateUserGroup(String userGroup) throws ServerException, LcmBadRequestException,
      AuthenticationException, JsonProcessingException {
    Entity<String> payload =
        Entity.entity(userGroup, "application/nl.kpmg.lcm.server.data.UserGroup+json");

    Invocation.Builder client = getClient("client/v0/userGroups");
    Response put = client.put(payload);

    checkForErrors(put);
  }

  public void deleteUserGroup(String userGroupId) throws AuthenticationException, ServerException,
       LcmBadRequestException {

    Invocation.Builder client = getClient(String.format("client/v0/userGroups/%s", userGroupId));
    Response delete = client.delete();

    checkForErrors(delete);
  }

  public LcmIdRepresentation getLcmId() throws AuthenticationException, ServerException,
      LcmBadRequestException {
    return getDatasRepresentation("client/v0/lcmId", LcmIdRepresentation.class);

  }

  public void createRemoteLcm(String storage) throws ServerException, LcmBadRequestException,
      AuthenticationException, JsonProcessingException {
    Entity<String> payload =
        Entity.entity(storage, "application/nl.kpmg.lcm.server.data.RemoteLcm+json");

    Invocation.Builder client = getClient("client/v0/remoteLcm");
    Response post = client.post(payload);

    checkForErrors(post);
  }

  public void updateRemoteLcm(String storage) throws ServerException, LcmBadRequestException,
      AuthenticationException, JsonProcessingException {
    Entity<String> payload =
        Entity.entity(storage, "application/nl.kpmg.lcm.server.data.RemoteLcm+json");

    Invocation.Builder client = getClient("client/v0/remoteLcm");
    Response put = client.put(payload);

    checkForErrors(put);
  }

  public void deleteRemoteLcm(String lcmId) throws AuthenticationException, ServerException,
       LcmBadRequestException {

    Invocation.Builder client = getClient(String.format("client/v0/remoteLcm/%s", lcmId));
    Response delete = client.delete();

    checkForErrors(delete);
  }

  public void addCertificateAlias(String alias, InputStream certificate)
      throws AuthenticationException, ServerException, LcmBadRequestException {
    editCertificateAlias(alias, certificate, false);
  }

  public void updateCertificateAlias(String alias, InputStream certificate)
      throws AuthenticationException, ServerException, LcmBadRequestException {
    editCertificateAlias(alias, certificate, true);
  }

  public void editCertificateAlias(String alias, InputStream certificate, boolean update)
      throws AuthenticationException, ServerException, LcmBadRequestException {

    Entity<InputStream> payload = Entity.entity(certificate, "application/octet-stream");

    Invocation.Builder client = getClient(String.format("client/v0/truststore/%s", alias));

    Response post;
    if (update) {
      post = client.put(payload);
    } else {
      post = client.post(payload);
    }

    checkForErrors(post);
  }

  public void createStorage(String storage) throws ServerException, LcmBadRequestException,
      AuthenticationException, JsonProcessingException {
    Entity<String> payload =
        Entity.entity(storage, "application/nl.kpmg.lcm.server.data.Storage+json");

    Invocation.Builder client = getClient("client/v0/storage");
    Response post = client.post(payload);

    checkForErrors(post);
  }

  public void updateStorage(String storage) throws ServerException, LcmBadRequestException,
      AuthenticationException, JsonProcessingException {
    Entity<String> payload =
        Entity.entity(storage, "application/nl.kpmg.lcm.server.data.Storage+json");

    Invocation.Builder client = getClient("client/v0/storage");
    Response put = client.put(payload);

    checkForErrors(put);
  }

  public void deleteStorage(String storageId) throws AuthenticationException, ServerException,
       LcmBadRequestException {

    Invocation.Builder client = getClient(String.format("client/v0/storage/%s", storageId));
    Response delete = client.delete();

    checkForErrors(delete);
  }

  public void createAuthorizedLcm(String authorizedLcm) throws ServerException, LcmBadRequestException,
      AuthenticationException, JsonProcessingException {
    Entity<String> payload =
        Entity.entity(authorizedLcm, "application/nl.kpmg.lcm.server.data.AuthorizedLcm+json");

    Invocation.Builder client = getClient("client/v0/authorizedlcm");
    Response post = client.post(payload);

    checkForErrors(post);
  }

    public void updateAuthorizedLcm(String authorizedLcm) throws ServerException, LcmBadRequestException,
      AuthenticationException, JsonProcessingException {
    Entity<String> payload =
        Entity.entity(authorizedLcm, "application/nl.kpmg.lcm.server.data.AuthorizedLcm+json");

    Invocation.Builder client = getClient("client/v0/authorizedlcm");
    Response put = client.put(payload);

    checkForErrors(put);
  }

  public void deleteAuthorizedLcm(String lcmId) throws AuthenticationException, ServerException,
       LcmBadRequestException {

    Invocation.Builder client = getClient(String.format("client/v0/authorizedlcm/%s", lcmId));
    Response delete = client.delete();

    checkForErrors(delete);
  }

  public TestResult testStorage(String storageId) {
    String path = "client/v0/storage/status/" + storageId;
    LOGGER.info(String.format("Executing test about storage on path: %s", path));
    try {
      Response response = getClient(path).get();
      if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
        TestResult result = response.readEntity(TestResult.class);
        return result;
      } else {
        String message =
            String.format("Test to to storage failed with: %d - %s", response.getStatus(), response
                .getStatusInfo().getReasonPhrase());
        LOGGER.info(message);
        return new TestResult(message, TestResult.TestCode.INACCESSIBLE);
      }
    } catch (AuthenticationException ex) {
      String message =
          String.format("Test to storage failed with authentication exception: %s ",
              ex.getMessage());
      LOGGER.info(message);
      return new TestResult(message, TestResult.TestCode.INACCESSIBLE);
    } catch (ServerException ex) {
      String message =
          String.format("Test to storage failed with server exception: %s ", ex.getMessage());
      LOGGER.info(message);
      return new TestResult(message, TestResult.TestCode.INACCESSIBLE);
    }
  }

  public void createUser(String user) throws ServerException, LcmBadRequestException,
      AuthenticationException, JsonProcessingException {
    Entity<String> payload =
        Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");

    Invocation.Builder client = getClient("client/v0/users");
    Response post = client.post(payload);

    checkForErrors(post);
  }

  public void updateUser(String user) throws ServerException, LcmBadRequestException,
      AuthenticationException, JsonProcessingException {
    Entity<String> payload =
        Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");

    Invocation.Builder client = getClient("client/v0/users");
    Response put = client.put(payload);

    checkForErrors(put);
  }

  public void deleteUser(String userId) throws AuthenticationException, ServerException,
       LcmBadRequestException {

    Invocation.Builder client = getClient(String.format("client/v0/users/%s", userId));
    Response delete = client.delete();

    checkForErrors(delete);
  }

  public void triggerTransfer(String remoLcmId, String remoteMetadataId, String jsonPayload)
      throws ServerException, LcmBadRequestException, AuthenticationException,
      JsonProcessingException {
    Entity<String> payload = Entity.entity(jsonPayload, "application/json");

    Invocation.Builder client =
        getClient("client/v0/remote/" + remoLcmId + "/metadata/" + remoteMetadataId);
    Response post = client.post(payload);

    checkForErrors(post);
  }

  public void postMetadata(String metadata)
      throws ServerException, LcmBadRequestException, AuthenticationException {
    Entity<String> payload =
        Entity.entity(metadata, "application/nl.kpmg.lcm.server.data.MetaData+json");

    Invocation.Builder client = getClient("client/v0/local");
    Response post = client.post(payload);

    checkForErrors(post);
  }

  public void putMetadata(String id, String metadata)
      throws AuthenticationException, ServerException, LcmBadRequestException {
    Entity<String> payload =
        Entity.entity(metadata, "application/nl.kpmg.lcm.server.data.MetaData+json");

    Invocation.Builder client = getClient(String.format("client/v0/local/%s", id));
    Response put = client.put(payload);

    checkForErrors(put);
  }

  public void deleteMetadata(String id)
      throws LcmBadRequestException, AuthenticationException, ServerException {
    Invocation.Builder client = getClient(String.format("client/v0/local/%s", id));
    Response delete = client.delete();

    checkForErrors(delete);
  }

  public void enrichMetadata(String metadataId)
      throws ServerException, LcmBadRequestException, AuthenticationException {
    Entity<String> payload =
        Entity.entity("", "application/nl.kpmg.lcm.server.data.EnrichmentProperties+json");

    Invocation.Builder client = getClient("client/v0/local/" + metadataId+ "/enrich");
    Response post = client.post(payload);

    checkForErrors(post);
  }

  public Set<String> getSubNamespaces(String baseNamespace)
      throws AuthenticationException, ServerException, LcmBadRequestException {
    String pathTemplate = "client/v0/local/namespace?namespace=%s";
    String path = String.format(pathTemplate, baseNamespace );
    LOGGER.info(String.format("Executing get on LCM-Server on path: %s", path));
    Response response = getClient(path).get();
    Set<String>  result = null;
    checkForErrors(response);
    
    result = response.readEntity(Set.class);
    return result;
    
  }

  public void deleteRemoteData(String metadataId, String lcmId) throws ServerException, AuthenticationException,
      LcmBadRequestException {
    String path = "client/v0/remoteData/" + metadataId;
    Map<String, String> parameters =  new HashMap();
    parameters.put("lcmId", lcmId);
    Invocation.Builder client = getClient(path, parameters);
    Response delete = client.delete();

    checkForErrors(delete);
  }

}