package nl.kpmg.lcm.server.rest.client;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.rest.client.version0.LocalMetaDataController;
import nl.kpmg.lcm.server.rest.client.version0.RemoteMetaDataController;
import nl.kpmg.lcm.server.rest.client.version0.TaskDescriptionController;
import nl.kpmg.lcm.server.rest.client.version0.TaskScheduleController;
import nl.kpmg.lcm.server.rest.client.version0.UserController;
import nl.kpmg.lcm.server.rest.client.version0.UserGroupController;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

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
     * The links the of a TaskDescription.
     */
    @InjectLinks({
        @InjectLink(
                resource = Backend.class,
                style = InjectLink.Style.ABSOLUTE,
                rel = "storage.overview",
                method = "getStorage"
        ),
        @InjectLink(
                resource = LocalMetaDataController.class,
                style = InjectLink.Style.ABSOLUTE,
                rel = "local.overview",
                method = "getLocalMetaDataOverview"
        ),
        @InjectLink(
                resource = RemoteMetaDataController.class,
                style = InjectLink.Style.ABSOLUTE,
                rel = "remote.overview",
                method = "getRemote"
        ),
        @InjectLink(
                resource = TaskDescriptionController.class,
                style = InjectLink.Style.ABSOLUTE,
                rel = "task.overview",
                method = "getOverview"
        ),
        @InjectLink(
                resource = TaskScheduleController.class,
                style = InjectLink.Style.ABSOLUTE,
                rel = "taskschedule.overview",
                method = "getCurrent"
        ),
        @InjectLink(
                resource = UserController.class,
                style = InjectLink.Style.ABSOLUTE,
                rel = "users.overview",
                method = "getUsers"
        ),
        @InjectLink(
                resource = UserGroupController.class,
                style = InjectLink.Style.ABSOLUTE,
                rel = "usergroups.overview",
                method = "getUserGroups"
        )
    })
    private List<Link> links;

    /**
     * Placeholder method for calls to a non endpoint.
     *
     * @return String "ok" as niceness
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Link> getIndex() {
    	return links;
    }
}
