package nl.kpmg.lcm.server.rest.client.version0;

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

import nl.kpmg.lcm.server.AuthenticationManager;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("client/v0/users")
public class UserController {
	
	private UserService userService;	
	private AuthenticationManager am;
	
	@Autowired
	public void setUserService(UserService userService){
		this.userService = userService;
	}
	
	@Autowired
	public void setAuthenticationManager(AuthenticationManager am){
		this.am = am;
	}
	
	@POST
	@Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
	@Produces({"application/json"})
	@Path("/login")
	public Response login(final User user, @QueryParam("serviceKey") String serviceKey) throws ServerException{
		String authorizationToken = am.getAuthentication(user.getUsername(), user.getPassword(), serviceKey);		
		return Response.status(200).entity(authorizationToken).build();		
	}
	
	@POST
	@Consumes({"application/json"})
	@Produces({"application/json"})
	@Path("/logout")
	public Response logout(@QueryParam("authorizationToken") String authorizationToken, @QueryParam("serviceKey") String serviceKey) throws ServerException{
			am.logout(serviceKey,authorizationToken);
			return Response.ok().entity("Logged Out Successfully.").build();		
	}
	
	@GET
	@Produces({"application/json"})	
	public Response getUsers(){		
		return Response.status(200).entity(userService.getUserDao().getUsers()).build();
	}
	
	@GET
	@Produces({"application/json"})
	@Path("/{username}")	
	public Response getUser(@PathParam("username") String username) {				
		return Response.status(200).entity(userService.getUserDao().getUser(username)).build();				
	}
	
	@PUT
	@Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
	@Path("/{username}")	
	public Response saveUser(final User user,@QueryParam("authourizationToken") String authourizationToken,@QueryParam("serviceKey") String serviceKey) throws ServerException{				
		userService.getUserDao().saveUser(user);
		return Response.status(200).entity("User "+user.getUsername() + " saved successfully.").build();				
	}
	
	@POST
	@Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
	@Produces({"application/nl.kpmg.lcm.server.data.User+json"})
	public Response modifyUser(final User user,@QueryParam("authourizationToken") String authourizationToken,@QueryParam("serviceKey") String serviceKey) throws ServerException{		
		
		userService.getUserDao().modifyUser(user);
		return Response.status(200).entity("Modified User "+user.getUsername()+" successfully.").build();		
	}
	
	@DELETE
	@Consumes({"application/json"})
	@Path("/{username}")
	public Response deleteUser(@PathParam("username") String username,@QueryParam("authourizationToken") String authourizationToken,@QueryParam("serviceKey") String serviceKey) throws ServerException{		
		userService.getUserDao().deleteUser(username);
		return Response.status(200).entity("Deleted User "+username+" Successfully.").build();		
	}
}
