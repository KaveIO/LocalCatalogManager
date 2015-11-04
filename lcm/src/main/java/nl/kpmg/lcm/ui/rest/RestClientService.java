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

import com.vaadin.server.VaadinService;
import com.vaadin.server.WrappedSession;
import java.util.List;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import nl.kpmg.lcm.server.rest.client.version0.types.MetaDataRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.MetaDatasRepresentation;
import nl.kpmg.lcm.ui.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author mhoekstra
 */
@Component
public class RestClientService {

    private String uri;

    @Autowired
    public RestClientService(Configuration configuration) {
        uri = String.format("http://%s:%s/", configuration.getServerName(), configuration.getServerPort());
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

    public void authenticate(String username, String password) throws AuthenticationException {
        Response post = ClientBuilder.newClient()
                .target(uri)
                .path("client/login")
                .request()
                .post(Entity.entity(
                        String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password),
                        "application/nl.kpmg.lcm.server.rest.client.types.LoginRequest+json"));

        int status = post.getStatus();
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

    public Invocation.Builder getClient(String path) throws AuthenticationException {
        if (!isAuthenticated()) {
            throw new AuthenticationException("Not logged in");
        }

        return ClientBuilder.newClient()
                .target(uri)
                .path(path)
                .request()
                .header("LCM-Authentication-User", retrieveLoginUser())
                .header("LCM-Authentication-Token", retrieveLoginToken());
    }

    public MetaDatasRepresentation getLocalMetadata() throws AuthenticationException {
        Invocation.Builder client = getClient("client/v0/local");

        Response response = client.get();
        MetaDatasRepresentation metaDatasRepresentation = response.readEntity(MetaDatasRepresentation.class);

        return metaDatasRepresentation;
    }
}
