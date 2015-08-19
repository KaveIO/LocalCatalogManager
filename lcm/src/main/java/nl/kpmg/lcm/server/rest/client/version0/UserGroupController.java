package nl.kpmg.lcm.server.rest.client.version0;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.UserGroup;
import nl.kpmg.lcm.server.data.service.UserGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("client/v0/userGroups")
public class UserGroupController {

	private UserGroupService userGroupService;
	//private AuthenticationManager am;

	@Autowired
	public void setUserGroupService(UserGroupService userGroupService) {
		this.userGroupService = userGroupService;
	}

	//@Autowired
	//public void setAuthenticationManager(AuthenticationManager am) {
	//	this.am = am;
	//}

	@GET
	@Produces({ "application/json" })	
	@RolesAllowed({"apiUser","administrator"})
	public Response getUserGroups() {

		return Response.status(200)
				.entity(userGroupService.getUserGroupDao().getUserGroups())
				.build();
	}

	@GET
	@Produces({ "application/json" })
	@Path("/{userGroup}")	
	@RolesAllowed({"apiUser","administrator"})
	public Response getUserGroup(@PathParam("userGroup") String userGroup) {

		return Response
				.status(200)
				.entity(userGroupService.getUserGroupDao().getUserGroup(
						userGroup)).build();
	}

	@PUT
	@Consumes({ "application/nl.kpmg.lcm.server.data.UserGroup+json" })
	@Path("/{userGroup}")	
	@RolesAllowed({"administrator"})
	public Response saveUserGroup(final UserGroup userGroup,
			@QueryParam("authourizationToken") String authourizationToken,
			@QueryParam("serviceKey") String serviceKey) throws ServerException {
		
		userGroupService.getUserGroupDao().saveUserGroup(userGroup);
		return Response.status(200).entity("Saved User Group "+userGroup.getUserGroup()+" Successfully.").build();
		
	}

	@POST
	@Consumes({ "application/nl.kpmg.lcm.server.data.UserGroup+json" })
	@Produces({ "text/plain" })
	@RolesAllowed({"administrator"})
	public Response modifyUserGroup(final UserGroup userGroup,
			@QueryParam("authourizationToken") String authourizationToken,
			@QueryParam("serviceKey") String serviceKey) throws ServerException {
			
		userGroupService.getUserGroupDao().modifyUserGroup(userGroup);
		return Response.status(200).entity("Modified User Group "+ userGroup.getUserGroup()+" Successfully.").build();		
	}

	@DELETE
	@Consumes({ "application/json" })
	@Path("/{userGroup}")
	@RolesAllowed({"administrator"})
	public Response deleteUserGroup(@PathParam("userGroup") String userGroup,
			@QueryParam("authourizationToken") String authourizationToken,
			@QueryParam("serviceKey") String serviceKey) throws ServerException {
		
		userGroupService.getUserGroupDao().deleteUserGroup(userGroup);
		return Response.status(200).entity("Deleted User Group "+userGroup+" Successfully.").build();
		
	}

}
