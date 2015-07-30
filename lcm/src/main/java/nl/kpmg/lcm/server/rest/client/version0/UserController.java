package nl.kpmg.lcm.server.rest.client.version0;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
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
	//private EncryptDecryptService encdecService;
	
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
		//@Context HttpHeaders httpHeaders
		//String authourizationToken = httpHeaders.getHeaderString("authourizationToken");
		if(am.isAuthorizationTokenValid(serviceKey, authorizationToken)){
			am.logout(serviceKey,authorizationToken);
			return Response.ok().build();
		} else {
			return Response.status(500).build();
		}
				
	}
	
	@GET
	@Produces({"application/json"})	
	public Response getUsers(){
		
		return Response.status(200).entity(userService.getUserDao().getUsers()).build();
	}
	
	@GET
	@Produces({"application/json"})
	@Path("/{username}")
	//@Authenticate(Role="USER")
	public Response getUser(@PathParam("username") String username, @QueryParam("authourizationToken") String authourizationToken,@QueryParam("serviceKey") String serviceKey) throws ServerException{
		if(am.isAuthorizationTokenValid(serviceKey, authourizationToken)){
		
		return Response.status(200).entity(userService.getUserDao().getUser(username)).build();
		} else {			
	        throw new NotFoundException(String.format("Service Key %s could not be found", serviceKey));	       			
		}
		//return Response.status(Status.UNAUTHORIZED).build();
	}
	
	@PUT
	@Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
	@Path("/{username}")
	//@Authenticate(Role="ADMIN")
	public Response saveUser(final User user){		
		userService.getUserDao().saveUser(user);
		return Response.status(200).build();
	}
	
	@POST
	@Consumes({"application/nl.kpmg.lcm.server.data.User+json"})
	@Produces({"application/nl.kpmg.lcm.server.data.User+json"})
	public Response modifyUser(final User user){		
		userService.getUserDao().modifyUser(user);
		return Response.status(200).build();
	}
	
	@DELETE
	@Consumes({"application/json"})
	@Path("/{username}")
	public Response deleteUser(@PathParam("username") String username){
		
		userService.getUserDao().deleteUser(username);
		return Response.status(200).build();
	}
}
