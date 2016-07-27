package nl.kpmg.lcm.server.rest.client.version0;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.dao.UserDao;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.authentication.UserPasswordHashException;
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

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

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
        return new UsersRepresentation(userService.findAll());
    }

    @GET
    @Produces({"application/nl.kpmg.lcm.server.rest.client.version0.types.UserRepresentation+json"})
    @Path("/{user_id}")
    @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
    public final Response getUser(@PathParam("user_id") String userId) {
        UserDao userDao = userService.getUserDao();

        User user = userDao.findOne(userId);
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
        userService.getUserDao().save(user);
        return Response.ok().build();
    }

    @PUT
    @Path("/{user_id}")
    @Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
    @RolesAllowed({Roles.ADMINISTRATOR})
    public final Response modifyUser(
            @PathParam("user_id") final String userId,
            final User user) {

        try {
            userService.updateUser(userId, user);
            return Response.ok().build();
        } catch (UserPasswordHashException ex) {
            logger.log(Level.SEVERE, "Password hashing failed during user modification", ex);
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("/{user_id}")
    @RolesAllowed({Roles.ADMINISTRATOR})
    public final Response deleteUser(@PathParam("user_id") final String userId) {
        UserDao userDao = userService.getUserDao();
        User user = userDao.findOne(userId);

        if (user != null) {
            userDao.delete(user);
            return Response.ok().build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}
