package nl.kpmg.lcm.server.rest;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import nl.kpmg.lcm.server.AuthenticationManager;

import org.springframework.beans.factory.annotation.Autowired;


@Provider
@PreMatching
public class LCMRESTRequestFilter implements ContainerRequestFilter {

	private static final Logger LOGGER = Logger.getLogger(LCMRESTRequestFilter.class.getName());
	private AuthenticationManager am;
		
	@Autowired
	public void setAuthenticationManager(AuthenticationManager am){
		this.am = am;
	}
	/** 
	 * Checks of Authorization Token for all URI's other than Login URI
	 * @see javax.ws.rs.container.ContainerRequestFilter#filter(javax.ws.rs.container.ContainerRequestContext)
	 */
	@Override
	public void filter(ContainerRequestContext requestContext)
			throws IOException {
		String path = requestContext.getUriInfo().getPath();
		LOGGER.log(Level.INFO,"LCMRESTRequestFilter called with request path "+path);
		if(requestContext.getRequest().getMethod().equals("OPTIONS")){
			requestContext.abortWith(Response.status(Response.Status.OK).build());
			return;
		}
		
		String serviceKey = requestContext.getUriInfo().getQueryParameters().getFirst("serviceKey");
		
		if(!am.isServiceKeyValid(serviceKey)){
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
			return;
		}
		
		if(!path.startsWith("client/v0/users/login")){
			String authorizationToken = requestContext.getUriInfo().getQueryParameters().getFirst("authorizationToken");	
			LOGGER.log(Level.INFO,"LCMRESTRequestFilter called with request authorizationToken "+authorizationToken);
			if(!am.isAuthorizationTokenValid(serviceKey, authorizationToken)){
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("You are not Authorized to access LCM").build());
			}
		}
	}	

}
