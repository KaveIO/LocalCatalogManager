package nl.kpmg.lcm.server.rest;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;


@Provider
@PreMatching
public class LCMRESTResponseFilter implements ContainerResponseFilter {

	private static final Logger LOGGER = Logger.getLogger(LCMRESTResponseFilter.class.getName());
	@Override
	public void filter(ContainerRequestContext requestContext,
			ContainerResponseContext responseContext) throws IOException {
		LOGGER.log(Level.INFO, "LCMRESTResponseFilter called");
		
		//responseContext.getHeaders().add("Acces-Control-Allow-Origin", "*");
		responseContext.getHeaders().add("Acces-Control-Allow-Credentials", "true");
		responseContext.getHeaders().add("Acces-Control-Allow-Methods", "POST,GET,PUT,DELETE");
		//responseContext.getHeaders().add("Acces-Control-Allow-Headers", "serviceKey"+", "+"authorizationToken");
		
	}

}
