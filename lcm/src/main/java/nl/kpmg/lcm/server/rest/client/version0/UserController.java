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
	@Produces({"text/plain"})
	@Path("/login")	
	public Response login(final User user, @QueryParam("serviceKey") String serviceKey) throws ServerException {
		String authorizationToken = am.getAuthentication(user.getName(), user.getPassword(), serviceKey);		
		return Response.status(200).entity(authorizationToken).build();		
	}
	
	@POST
	@Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
	@Produces({"text/plain"})
	@Path("/logout")	
	public Response logout(final User user,@QueryParam("authorizationToken") String authorizationToken, @QueryParam("serviceKey") String serviceKey) throws ServerException{
			am.logout(user.getName(),serviceKey,authorizationToken);
			return Response.ok().entity("Logged Out user "+user.getName()+" Successfully.").build();		
	}
	
	@GET
	@Produces({"application/json"})	
	@RolesAllowed({"apiUser","administrator"})	
	public Response getUsers(){		
		return Response.status(200).entity(userService.getUserDao().getAll()).build();
	}
	
	@GET
	@Produces({"application/json"})
	@Path("/{id}")
	@RolesAllowed({"apiUser","administrator"})
	public Response getUser(@PathParam("id") Integer id) {				
		return Response.status(200).entity(userService.getUserDao().getById(id)).build();				
	}
	
	@PUT
	@Consumes({"application/nl.kpmg.lcm.server.data.User+json"})	
	@RolesAllowed({"administrator"})
	public Response saveUser(final User user,@QueryParam("authourizationToken") String authourizationToken,@QueryParam("serviceKey") String serviceKey) throws ServerException{				
		userService.getUserDao().persist(user);
		return Response.status(200).entity("User "+user.getName() + " saved successfully.").build();				
	}
	
	@POST
	@Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
	@Produces({"text/plain"})
	@RolesAllowed({"administrator"})
	public Response modifyUser(final User user,@QueryParam("authourizationToken") String authourizationToken,@QueryParam("serviceKey") String serviceKey) throws ServerException{		
		
		userService.getUserDao().update(user);
		return Response.status(200).entity("Modified User "+user.getName()+" successfully.").build();		
	}
	
	@DELETE
	@Consumes({"application/json"})
	@Produces({"text/plain"})
	@Path("/{id}")
	@RolesAllowed({"apiUser","administrator"})
	public Response deleteUser(@PathParam("id") Integer id, @QueryParam("authourizationToken") String authourizationToken,@QueryParam("serviceKey") String serviceKey) throws ServerException{				
		User user = userService.getUserDao().getById(id);
		if(user == null){
			return Response.status(404).entity("User "+id+" doesn't exist.").build();
		}
		userService.getUserDao().delete(user);
		return Response.status(200).entity("Deleted User "+user.getName()+" Successfully.").build();		
	}
}
