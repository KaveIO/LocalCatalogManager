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

import nl.kpmg.lcm.server.rest.authentication.SessionAuthenticationManager;
import nl.kpmg.lcm.server.LoginException;
import nl.kpmg.lcm.server.LogoutException;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.authentication.RequestFilter;
import nl.kpmg.lcm.server.rest.types.LoginRequest;

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
     * Default constructor.
     *
     * @param userService providing user DAO access
     */
    @Autowired
    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @GET
    @Produces({"application/json"})
    @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
    public Response getUsers() {
        return Response.status(200).entity(userService.getUserDao().getUsers()).build();
    }

    @GET
    @Produces({"application/json"})
    @Path("/{username}")
    @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
    public Response getUser(@PathParam("username") String username) {
        return Response.status(200).entity(userService.getUserDao().getUser(username)).build();
    }

    @PUT
    @Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
    @Path("/{username}")
    @RolesAllowed({Roles.ADMINISTRATOR})
    public Response saveUser(final User user, @QueryParam("authourizationToken") String authourizationToken, @QueryParam("serviceKey") String serviceKey) throws ServerException {
        userService.getUserDao().saveUser(user);
        return Response.status(200).entity("User " + user.getUsername() + " saved successfully.").build();
    }

    @POST
    @Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
    @Produces({"text/plain"})
    @RolesAllowed({Roles.ADMINISTRATOR})
    public Response modifyUser(final User user, @QueryParam("authourizationToken") String authourizationToken, @QueryParam("serviceKey") String serviceKey) throws ServerException {

        userService.getUserDao().modifyUser(user);
        return Response.status(200).entity("Modified User " + user.getUsername() + " successfully.").build();
    }

    @DELETE
    @Consumes({"application/json"})
    @Path("/{username}")
    @RolesAllowed({Roles.ADMINISTRATOR})
    public Response deleteUser(@PathParam("username") String username, @QueryParam("authourizationToken") String authourizationToken, @QueryParam("serviceKey") String serviceKey) throws ServerException {
        userService.getUserDao().deleteUser(username);
        return Response.status(200).entity("Deleted User " + username + " Successfully.").build();
    }
}
