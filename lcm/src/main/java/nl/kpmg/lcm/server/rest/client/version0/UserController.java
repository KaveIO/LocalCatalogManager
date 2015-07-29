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
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.service.EncryptDecryptService;
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
	
	@GET
	@Produces({"application/json"})	
	public Response getUsers(){
		
		return Response.status(200).entity(userService.getUserDao().getUsers()).build();
	}
	
	@GET
	@Produces({"application/json"})
	@Path("/{username}")
	//@Authenticate(Role="USER")
	public Response getUser(@PathParam("username") String username,@QueryParam("serviceKey") String serviceKey){
		am.getAuthentication(username, "admin", serviceKey);
		if(am.isAuthenticated()){
		return Response.status(200).entity(userService.getUserDao().getUser(username)).build();
		} else {
			return Response.status(403).build();
		}
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
