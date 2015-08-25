package nl.kpmg.lcm.server.rest.client.version0;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import nl.kpmg.lcm.server.AuthenticationManager;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.LogoutException;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.LCMRESTRequestFilter;
import nl.kpmg.lcm.server.rest.client.version0.types.LoginRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Controller for basic user operations, this also contains login in and logout.
 *
 * @author mhoekstra
 */
@Component
@Path("client/v0/users")
public class UserController {
    /**
     * The user service.
     */
    private final UserService userService;

    /**
     * The authentication manager.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Default constructor.
     *
     * @param userService providing user DAO access
     * @param authenticationManager for authentication of users
     */
    @Autowired
    public UserController(final UserService userService, final AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Tries to log in based on provided credentials.
     *
     * @param loginRequest request containing username and password
     * @return Authorization token if successful. status 400 if not.
     */
    @POST
    @Consumes({"application/nl.kpmg.lcm.server.rest.client.version0.types.LoginRequest+json" })
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
            @HeaderParam(LCMRESTRequestFilter.LCM_AUTHENTICATION_TOKEN_HEADER) final String authenticationToken) {
        try {
            authenticationManager.logout(authenticationToken);
            return Response.ok().build();
        } catch (LogoutException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("logout unsuccessful").build();
        }
    }

    @GET
    @Produces({"application/json"})
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR, AuthenticationManager.Roles.API_USER})
    public Response getUsers() {
        return Response.status(200).entity(userService.getUserDao().getUsers()).build();
    }

    @GET
    @Produces({"application/json"})
    @Path("/{username}")
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR, AuthenticationManager.Roles.API_USER})
    public Response getUser(@PathParam("username") String username) {
        return Response.status(200).entity(userService.getUserDao().getUser(username)).build();
    }

    @PUT
    @Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
    @Path("/{username}")
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR})
    public Response saveUser(final User user, @QueryParam("authourizationToken") String authourizationToken, @QueryParam("serviceKey") String serviceKey) throws ServerException {
        userService.getUserDao().saveUser(user);
        return Response.status(200).entity("User " + user.getUsername() + " saved successfully.").build();
    }

    @POST
    @Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
    @Produces({"text/plain"})
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR})
    public Response modifyUser(final User user, @QueryParam("authourizationToken") String authourizationToken, @QueryParam("serviceKey") String serviceKey) throws ServerException {

        userService.getUserDao().modifyUser(user);
        return Response.status(200).entity("Modified User " + user.getUsername() + " successfully.").build();
    }

    @DELETE
    @Consumes({"application/json"})
    @Path("/{username}")
    @RolesAllowed({AuthenticationManager.Roles.ADMINISTRATOR})
    public Response deleteUser(@PathParam("username") String username, @QueryParam("authourizationToken") String authourizationToken, @QueryParam("serviceKey") String serviceKey) throws ServerException {
        userService.getUserDao().deleteUser(username);
        return Response.status(200).entity("Deleted User " + username + " Successfully.").build();
    }
}
