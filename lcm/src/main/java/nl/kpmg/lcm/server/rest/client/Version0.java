package nl.kpmg.lcm.server.rest.client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * First Implementation of the LCM Client interface.
 *
 * The client interface is the REST API to which all clients will make their
 * requests.
 *
 * @author mhoekstra
 */
@Path("client/v0")
public class Version0 {

    /**
     * Placeholder method for calls to a non endpoint.
     *
     * @return String "ok" as niceness
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIndex() {
        return "ok";
    }
}
