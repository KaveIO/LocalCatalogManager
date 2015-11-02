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
package nl.kpmg.lcm.server.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.LogoutException;
import nl.kpmg.lcm.server.rest.authentication.SessionAuthenticationManager;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.client.types.ClientRepresentation;
import nl.kpmg.lcm.server.rest.client.types.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author mhoekstra
 */
@Path("client")
public class Client {

    /**
     * The authentication manager.
     */
    private final SessionAuthenticationManager authenticationManager;

    /**
     * Default constructor.
     *
     * @param userService providing user DAO access
     * @param authenticationManager for authentication of users
     */
    @Autowired
    public Client(final UserService userService, final SessionAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * Method returning the versions of the client interface available.
     * The returned object will be sent to the client as "application/json"
     * media type.
     *
     * @return String that will be returned as a application/json response.
     */
    @GET
    @Produces({"application/json" })
    public final ClientRepresentation getIndex() {
        return new ClientRepresentation();
    }

        /**
     * Tries to log in based on provided credentials.
     *
     * @param loginRequest request containing username and password
     * @return Authorization token if successful. status 400 if not.
     */
    @POST
    @Consumes({"application/nl.kpmg.lcm.server.rest.client.types.LoginRequest+json" })
    @Produces({"text/plain" })
    @Path("/login")
    public final Response login(final LoginRequest loginRequest) {
        String authorizationToken;
        try {
            authorizationToken = authenticationManager.getAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword());
            return Response.ok().entity(authorizationToken).build();
        } catch (LoginException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("login unsuccessful").build();
        }
    }

    /**
     * Logs the current user out.
     *
     * @param authenticationToken provided via the header
     * @return 200 if successful, 400 Bad Request if the user couldn't be logged out
     */
    @POST
    @Produces({"text/plain" })
    @Path("/logout")
    public final Response logout(
            @HeaderParam(SessionAuthenticationManager.LCM_AUTHENTICATION_TOKEN_HEADER) final String authenticationToken) {
        try {
            authenticationManager.removeAuthenticationToken(authenticationToken);
            return Response.ok().build();
        } catch (LogoutException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("logout unsuccessful").build();
        }
    }
}