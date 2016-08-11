package nl.kpmg.lcm.server;


import nl.kpmg.lcm.configuration.ServerConfiguration;

import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.kpmg.lcm.HTTPServerProvider;
import nl.kpmg.lcm.server.rest.authentication.RequestFilter;
import nl.kpmg.lcm.server.rest.authentication.ResponseFilter;
import nl.kpmg.lcm.server.task.TaskManager;
import nl.kpmg.lcm.server.task.TaskManagerException;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import nl.kpmg.lcm.RedirectServer;
import nl.kpmg.lcm.server.rest.authentication.Roles;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.linking.DeclarativeLinkingFeature;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.message.filtering.SecurityAnnotations;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author mhoekstra
 */
public class Server {
  private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

  private ServerConfiguration configuration;
  private ApplicationContext context;

  private String baseUri;
  private String baseFallbackUri;

  private HttpServer restServer;
  private TaskManager taskManager;

  public Server() throws ServerException {
    // A double application conext is used because the single configuration
    // file (application.properties) drives which application Context is
    // actually effective. Effort was made to enable a hierachical application
    // context. However this approach failed. Currently we load the
    // parentContext just for the Configuration bean. Moments later we load
    // the actuall ApplicationContext and ignore the ParentContext.
    ApplicationContext parentContext =
        new ClassPathXmlApplicationContext(new String[] {"application-context-server.xml"});
    configuration = parentContext.getBean(ServerConfiguration.class);

    String storage = configuration.getServerStorage();
    baseUri = String.format("%s://%s:%d/", "https", configuration.getServiceName(),
        configuration.getSecureServicePort());
    baseFallbackUri = String.format("%s://%s:%d/", "http", configuration.getServiceName(),
        configuration.getServicePort());

    // Switching the application context based on the configuration. The configuration
    // allows for different storage backend which are Autwired with Spring.
    switch (storage) {
      case "file":
        LOGGER.log(Level.INFO, "Loading file based storage ApplicationContext");
        context = new ClassPathXmlApplicationContext(
            new String[] {"application-context-server-file.xml"});
        break;
      case "mongo":
        LOGGER.log(Level.INFO, "Loading mongo based storage ApplicationContext");
        context = new ClassPathXmlApplicationContext(
            new String[] {"application-context-server-mongo.xml"});
        break;
      default:
        throw new ServerException("Couldn't determine LCM storage engine.");
    }
  }

  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   *
   * @return Grizzly HTTP server.
   */
  public HttpServer startRestInterface() throws ServerException {
    // create a resource config that scans for JAX-RS resources and providers
    // in nl.kpmg.lcm.server.rest
    final ResourceConfig rc =
        new ResourceConfig().packages("nl.kpmg.lcm.server.rest").property("contextConfig", context)
            .property(EntityFilteringFeature.ENTITY_FILTERING_SCOPE,
                new Annotation[] {SecurityAnnotations
                    .rolesAllowed(new String[] {Roles.ADMINISTRATOR, Roles.API_USER})})
            .register(JacksonFeature.class).register(JacksonJsonProvider.class)
            .register(RequestFilter.class).register(ResponseFilter.class)
            .register(DeclarativeLinkingFeature.class).register(UriBuilderEntityProcessor.class)
            .register(LoggingExceptionMapper.class);

    return HTTPServerProvider.createHTTPServer(configuration, baseUri, baseFallbackUri, rc, true);
  }

  public TaskManager startTaskManager() throws TaskManagerException {
    TaskManager taskManager = new TaskManager();
    taskManager.initialize(context);
    return taskManager;
  }

  /**
   * Main method.
   *
   * @throws ServerException
   */
  public void start() throws ServerException {
    try {
      restServer = startRestInterface();
      taskManager = startTaskManager();
    } catch (TaskManagerException ex) {
      Logger.getLogger(RedirectServer.class.getName()).log(Level.SEVERE,
          "Failed starting the LocalCatalogManager due to the TaskManager", ex);
      throw new ServerException(ex);
    }
  }

  public void stop() {
    restServer.shutdownNow();
    taskManager.stop();
  }

  public String getBaseUri() {
    return baseUri;
  }

}
