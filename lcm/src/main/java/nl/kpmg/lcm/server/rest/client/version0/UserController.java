package nl.kpmg.lcm.server.rest.client.version0;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.dao.UserDao;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.client.version0.types.UserRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.UsersRepresentation;
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

    /**
     * Returns all the registered users in the system.
     *
     * @return list of all the users
     */
    @GET
    @Produces({"application/nl.kpmg.lcm.server.rest.client.version0.types.UsersRepresentation+json"})
    @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
    public final UsersRepresentation getUsers() {
        UserDao userDao = userService.getUserDao();
        return new UsersRepresentation(userDao.getUsers());
    }

    @GET
    @Produces({"application/nl.kpmg.lcm.server.rest.client.version0.types.UserRepresentation+json"})
    @Path("/{user_id}")
    @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
    public final Response getUser(@PathParam("user_id") String user_id) {
        UserDao userDao = userService.getUserDao();

        User user = userDao.getUser(user_id);

        if (user != null) {
            return Response.ok(new UserRepresentation(user)).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @POST
    @Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
    @RolesAllowed({Roles.ADMINISTRATOR})
    public final Response createNewUser(final User user) {
        userService.getUserDao().saveUser(user);
        return Response.ok().build();
    }

    @POST
    @Path("/{user_id}")
    @Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
    @RolesAllowed({Roles.ADMINISTRATOR})
    public final Response modifyUser(final User user) {
        userService.getUserDao().modifyUser(user);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{user_id}")
    @RolesAllowed({Roles.ADMINISTRATOR})
    public final Response deleteUser(@PathParam("user_id") final String userId) {
        UserDao userDao = userService.getUserDao();
        User user = userDao.getUser(userId);

        if (user != null) {
            userDao.deleteUser(userId);
            return Response.ok().build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}
