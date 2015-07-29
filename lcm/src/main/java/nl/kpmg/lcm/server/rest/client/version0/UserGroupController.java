package nl.kpmg.lcm.server.rest.client.version0;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import nl.kpmg.lcm.server.data.UserGroup;
import nl.kpmg.lcm.server.data.service.UserGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("client/v0/userGroups")
public class UserGroupController {
		
	private UserGroupService userGroupService;
	
	@Autowired
	public void setUserGroupService(UserGroupService userGroupService){
		this.userGroupService = userGroupService;
	}
	
	@GET
	@Produces({"application/json"})	
	public Response getUserGroups(){
		
		return Response.status(200).entity(userGroupService.getUserGroupDao().getUserGroups()).build();
	}
	
	@GET
	@Produces({"application/json"})
	@Path("/{userGroup}")
	//@Authenticate(Role="USER")
	public Response getUserGroup(@PathParam("userGroup") String userGroup){
		
		return Response.status(200).entity(userGroupService.getUserGroupDao().getUserGroup(userGroup)).build();
	}
	
	@PUT
	@Consumes({"application/nl.kpmg.lcm.server.data.UserGroup+json"})
	@Path("/{userGroup}")
	//@Authenticate(Role="ADMIN")
	public Response saveUserGroup(final UserGroup userGroup){		
		userGroupService.getUserGroupDao().saveUserGroup(userGroup);
		return Response.status(200).build();
	}
	
	@POST
	@Consumes({"application/nl.kpmg.lcm.server.data.UserGroup+json"})
	@Produces({"application/nl.kpmg.lcm.server.data.UserGroup+json"})
	public Response modifyUserGroup(final UserGroup userGroup){		
		userGroupService.getUserGroupDao().modifyUserGroup(userGroup);
		return Response.status(200).build();
	}
	
	@DELETE
	@Consumes({"application/json"})
	@Path("/{userGroup}")
	public Response deleteUserGroup(@PathParam("userGroup") String userGroup){
		
		userGroupService.getUserGroupDao().deleteUserGroup(userGroup);
		return Response.status(200).build();
	}	

}
