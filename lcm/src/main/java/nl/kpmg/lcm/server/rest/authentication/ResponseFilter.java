package nl.kpmg.lcm.server.rest.authentication;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * Filter placed on all REST responses that modifies the headers sent to the client.
 *
 * @author mhoekstra
 */
@Provider
@PreMatching
public class ResponseFilter implements ContainerResponseFilter {

    /**
     * The class logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ResponseFilter.class.getName());

    /**
     * Filters the responses and adds the appropriate authentication headers.
     *
     * @param requestContext the request
     * @param responseContext the response
     */
    @Override
    public final void filter(final ContainerRequestContext requestContext,
            final ContainerResponseContext responseContext) {
        LOGGER.log(Level.FINE, "LCMRESTResponseFilter called with Entity {0}", responseContext.getEntity());

        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        headers.add("Acces-Control-Allow-Headers",
                String.format("%s, %s",
                SessionAuthenticationManager.LCM_AUTHENTICATION_USER_HEADER,
                SessionAuthenticationManager.LCM_AUTHENTICATION_TOKEN_HEADER));
        headers.add("WWW-Authenticate", "Basic realm=\"LCM\", LCMToken realm=\"LCM\"");
    }
}
