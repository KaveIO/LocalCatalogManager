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
        UserGroupDao userGroupDao = userGroupService.getUserGroupDao();
        List<UserGroup> userGroups = userGroupDao.getUserGroups();

        return new UserGroupsRepresentation(userGroups);
    }

    @GET
    @Path("/{usergroup_id}")
    @Produces({"application/nl.kpmg.lcm.server.rest.client.version0.types.UserGroupRepresentation+json"})
    @RolesAllowed({Roles.ADMINISTRATOR, Roles.API_USER})
    public final Response getUserGroup(@PathParam("usergroup_id") String userGroupId) {
        UserGroupDao userGroupDao = userGroupService.getUserGroupDao();
        UserGroup userGroup = userGroupDao.getUserGroup(userGroupId);

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
        userGroupDao.saveUserGroup(userGroup);

        return Response.ok().build();
    }

    @PUT
    @Path("/{usergroup_id}")
    @Consumes({"application/nl.kpmg.lcm.server.data.UserGroup+json"})
    @RolesAllowed({Roles.ADMINISTRATOR})
    public final Response modifyUserGroup(final UserGroup userGroup) {
        userGroupService.getUserGroupDao().modifyUserGroup(userGroup);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{usergroup_id}")
    @RolesAllowed({Roles.ADMINISTRATOR})
    public final Response deleteUserGroup(@PathParam("usergroup_id") String userGroupId) {
        UserGroupDao userGroupDao = userGroupService.getUserGroupDao();
        userGroupDao.deleteUserGroup(userGroupId);
        return Response.ok().build();
    }
}
