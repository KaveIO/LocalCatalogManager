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
package nl.kpmg.lcm.ui.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.server.VaadinService;
import com.vaadin.server.WrappedSession;

import nl.kpmg.lcm.client.Configuration;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.rest.client.version0.types.MetaDatasRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.StoragesRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.TaskDescriptionsRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.TaskScheduleRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.UserGroupsRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.UsersRepresentation;
import nl.kpmg.lcm.ui.Client;
import nl.kpmg.lcm.ui.UI;

/**
 *
 * @author mhoekstra
 */
@Component
public class RestClientService {

    private static final Logger LOGGER = Logger.getLogger(UI.class.getName());
	
    private String uri;
    private String unsafeUri;
    
    private Client client;
    
    private boolean secure = true;

    @Autowired
    public RestClientService(Configuration configuration) {
        uri = String.format("https://%s:%s/", configuration.getServiceName(), configuration.getTargetPort());
        unsafeUri = String.format("http://%s:%s/", configuration.getServiceName(), configuration.getUnsafeTargetPort());
        client = new Client();
    }

    private void saveLoginToken(String loginToken) {
        VaadinService.getCurrentRequest().getWrappedSession()
                .setAttribute("loginToken", loginToken);
    }

    private void saveLoginUser(String loginUser) {
        VaadinService.getCurrentRequest().getWrappedSession()
                .setAttribute("loginUser", loginUser);
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
    			LOGGER.log(Level.WARNING, "URIooooo: "+uri);
    		return client.createWebTarget(uri)
	                 .path(path)
	                 .request()
	                 .post(payload);
    }
    
    public void authenticate(String username, String password) throws AuthenticationException, ServerException {
        String path = "client/login";
        Entity<String> payload = Entity.entity(
                String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password),
                "application/nl.kpmg.lcm.server.rest.client.types.LoginRequest+json");
        Response post;
    		try { post = post(uri, path, payload); } catch(ServerException | ProcessingException e) {
    			LOGGER.log(Level.WARNING, "Server error in LCM target server HTTPS REST invocation, trying HTTP...", e);
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
        return retrieveLoginToken != null
                && !retrieveLoginToken.isEmpty()
                && retrieveLoginUser != null
                && !retrieveLoginUser.isEmpty();
    }

    public Invocation.Builder getClient(String path) throws AuthenticationException, ServerException {
        if (!isAuthenticated()) {
            throw new AuthenticationException("Not logged in");
        }

        String uri;
        if (secure) {
        		uri = this.uri;
        }
        else {
        		uri = this.unsafeUri;
        }
        
        return client.createWebTarget(uri)
                .path(path)
                .request()
                .header("LCM-Authentication-User", retrieveLoginUser())
                .header("LCM-Authentication-Token", retrieveLoginToken());
    }

    public MetaDatasRepresentation getLocalMetadata() throws AuthenticationException, ServerException {
        Invocation.Builder client = getClient("client/v0/local");

        Response response = client.get();
        MetaDatasRepresentation metaDatasRepresentation = response.readEntity(MetaDatasRepresentation.class);

        return metaDatasRepresentation;
    }

    public StoragesRepresentation getStorage() throws AuthenticationException, ServerException {
        Invocation.Builder client = getClient("client/v0/storage");

        Response response = client.get();
        StoragesRepresentation storagesRepresentation = response.readEntity(StoragesRepresentation.class);

        return storagesRepresentation;
    }

    public TaskDescriptionsRepresentation getTasks() throws AuthenticationException, ServerException {
        Invocation.Builder client = getClient("client/v0/tasks");

        Response response = client.get();
        TaskDescriptionsRepresentation taskDescriptionsRepresentation = response.readEntity(TaskDescriptionsRepresentation.class);

        return taskDescriptionsRepresentation;
    }

    public TaskScheduleRepresentation getTaskSchedule() throws AuthenticationException, ServerException {
        Invocation.Builder client = getClient("client/v0/taskschedule");

        Response response = client.get();
        TaskScheduleRepresentation taskScheduleRepresentation = response.readEntity(TaskScheduleRepresentation.class);

        return taskScheduleRepresentation;
    }

    public UsersRepresentation getUsers() throws AuthenticationException, ServerException {
        Invocation.Builder client = getClient("client/v0/users");

        Response response = client.get();
        UsersRepresentation usersRepresentation = response.readEntity(UsersRepresentation.class);

        return usersRepresentation;
    }

    public UserGroupsRepresentation getUserGroups() throws AuthenticationException, ServerException {
        Invocation.Builder client = getClient("client/v0/userGroups");

        Response response = client.get();
        UserGroupsRepresentation userGroupsRepresentation = response.readEntity(UserGroupsRepresentation.class);

        return userGroupsRepresentation;
    }
}
