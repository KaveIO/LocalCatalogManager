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
    @Produces(MediaType.TEXT_HTML)
    public String getIndex() {
        String index = "<html><head><title>Welcome to LCM</title></head><body><p><h1>Local Catalog Manager</h1></p><form name=\"loginForm\" method=\"GET\">User Name  :<input id=\"username\" type=\"text\" value=\"Enter User Name\"></input> <br> Password     :<input id=\"password\" type=\"password\"></input><br><input id=\"submit\" type=\"button\" value=\"Login\"></input></form></body></html>";
    	
    	return index;
    }
}
