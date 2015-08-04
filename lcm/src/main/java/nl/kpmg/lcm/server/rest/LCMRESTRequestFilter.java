package nl.kpmg.lcm.server.rest;

import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
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
	public void filter(final ContainerRequestContext requestContext)
			throws IOException {
		requestContext.setSecurityContext(new LCMSecurityContext());		
		
		String path = requestContext.getUriInfo().getPath();
		LOGGER.log(Level.INFO,"LCMRESTRequestFilter called with request path "+path);
		if(requestContext.getRequest().getMethod().equals("OPTIONS")){
			requestContext.abortWith(Response.status(Response.Status.OK).entity("You are not allowed to query OPTIONS.").build());
			return;
		}
		
		String serviceKey = requestContext.getUriInfo().getQueryParameters().getFirst("serviceKey");
		
		if(!am.isServiceKeyValid(serviceKey)){
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("You are not Authorized to access LCM due to invalid service key "+serviceKey).build());
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
	
	private final class LCMSecurityContext implements SecurityContext {
		@Override
		public Principal getUserPrincipal() {				
			return new Principal(){

				@Override
				public String getName() {						
					return "lcm";
				}};
		}

		@Override
		public boolean isUserInRole(String role) {
			LOGGER.log(Level.INFO,"LCMRESTRequestFilter::LCMSecurityContext called with role "+role);
			return "administrator".equals(role);
		}

		@Override
		public boolean isSecure() {				
			return false;
		}

		@Override
		public String getAuthenticationScheme() {				
			return null;
		}
	}
		
}
