package nl.kpmg.lcm.server.rest.client.version0;

import java.util.List;
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
import nl.kpmg.lcm.server.data.UserGroup;
import nl.kpmg.lcm.server.data.dao.UserGroupDao;
import nl.kpmg.lcm.server.data.service.UserGroupService;
import nl.kpmg.lcm.server.rest.client.version0.types.UserGroupRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.UserGroupsRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("client/v0/userGroups")
public class UserGroupController {

    private final UserGroupService userGroupService;

    @Autowired
    public UserGroupController(final UserGroupService userGroupService) {
        this.userGroupService = userGroupService;
    }

    @GET
    @Produces({"application/nl.kpmg.lcm.server.rest.client.version0.types.UserGroupsRepresentation+json"})
    @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
    public final UserGroupsRepresentation getUserGroups() {
        List<UserGroup> userGroups = userGroupService.findAll();

        return new UserGroupsRepresentation(userGroups);
    }

    @GET
    @Path("/{user_group_id}")
    @Produces({"application/nl.kpmg.lcm.server.rest.client.version0.types.UserGroupRepresentation+json"})
    @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
    public final Response getUserGroup(@PathParam("user_group_id") String userGroupId) {
        UserGroupDao userGroupDao = userGroupService.getUserGroupDao();
        UserGroup userGroup = userGroupDao.findOne(userGroupId);

        if (userGroup != null) {
            return Response.ok(new UserGroupRepresentation(userGroup)).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @POST
    @Consumes({"application/nl.kpmg.lcm.server.data.UserGroup+json"})
    @RolesAllowed({Roles.ADMINISTRATOR})
    public final Response createNewUserGroup(final UserGroup userGroup) {
        UserGroupDao userGroupDao = userGroupService.getUserGroupDao();
        userGroupDao.save(userGroup);

        return Response.ok().build();
    }

    @PUT
    @Path("/{user_group_id}")
    @Consumes({"application/nl.kpmg.lcm.server.data.UserGroup+json"})
    @RolesAllowed({Roles.ADMINISTRATOR})
    public final Response modifyUserGroup(
            @PathParam("user_group_id") final String userGroupId,
            final UserGroup userGroup) {
        userGroupService.getUserGroupDao().save(userGroup);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{user_group_id}")
    @RolesAllowed({Roles.ADMINISTRATOR})
    public final Response deleteUserGroup(
            @PathParam("user_group_id") final String userGroupId) {

        UserGroupDao userGroupDao = userGroupService.getUserGroupDao();
        UserGroup userGroup = userGroupDao.findOne(userGroupId);
        if (userGroup != null) {
            userGroupDao.delete(userGroup);
            return Response.ok().build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}
