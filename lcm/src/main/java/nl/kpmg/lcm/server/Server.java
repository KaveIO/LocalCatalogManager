package nl.kpmg.lcm.server;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.task.TaskManager;
import nl.kpmg.lcm.server.task.TaskManagerException;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.linking.DeclarativeLinkingFeature;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author mhoekstra
 */

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private Configuration configuration;
    private ApplicationContext context;

    private final String baseUri;

    private HttpServer restServer;
    private TaskManager taskManager;

    public Server(Configuration configuration) throws ServerException {
        this.configuration = configuration;

        baseUri = String.format(
                "http://%s:%s/",
                configuration.getServerName(),
                configuration.getServerPort());
        String storage = configuration.getServerStorage();

        // Switching the application context based on the configuration. The configuration
        // allows for different storage backend which are Autwired with Spring.
        switch (storage) {
            case "file":
                LOGGER.log(Level.INFO, "Loading file based storage ApplicationContext");
                context = new ClassPathXmlApplicationContext(new String[] {
                    "application-context-file.xml"
                }); break;
            case "mongo":
                LOGGER.log(Level.INFO, "Loading mongo based storage ApplicationContext");
                context = new ClassPathXmlApplicationContext(new String[] {
                    "application-context-mongo.xml"
                }); break;
            default:
                throw new ServerException("Couldn't determine LCM storage engine.");
        }


        System.out.println("Mongo DB : " + context.getBean("metaDataDao"));
    }

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public HttpServer startRestInterface() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc = new ResourceConfig()
                .packages("nl.kpmg.lcm.server.rest")
                .registerClasses(JacksonFeature.class)
                .registerClasses(JacksonJsonProvider.class)
                .registerClasses(LoggingExceptionMapper.class)
                .register(DeclarativeLinkingFeature.class)
                .property("contextConfig", context);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), rc);
    }

    public TaskManager startTaskManager() throws TaskManagerException {
        TaskManager taskManager = TaskManager.getInstance();
        taskManager.initialize(context);
        return taskManager;
    }

    /**
     * Main method.
     * @throws ServerException
     */
    public void start() throws ServerException  {
        try {
            restServer = startRestInterface();
            taskManager = startTaskManager();
        } catch (TaskManagerException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Failed starting the LocalCatalogManager due to the TaskManager", ex);
            throw new ServerException(ex);
        }
    }

    public void stop() {
        restServer.stop();
        taskManager.stop();
    }

    public String getBaseUri() {
        return baseUri;
    }
}
