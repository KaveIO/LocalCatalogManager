/*
 * Copyright 2016 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package nl.kpmg.lcm.server;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import nl.kpmg.lcm.common.GeneralExceptionMapper;
import nl.kpmg.lcm.common.HttpsServerProvider;
import nl.kpmg.lcm.common.HttpsServerWrapper;
import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.SslConfigurationException;
import nl.kpmg.lcm.common.configuration.ServerConfiguration;
import nl.kpmg.lcm.server.data.service.LcmIdService;
import nl.kpmg.lcm.server.exception.mapper.LcmExceptionMapper;
import nl.kpmg.lcm.server.exception.mapper.ValidationExceptionMapper;
import nl.kpmg.lcm.server.rest.authentication.AuthenticationRequestFilter;
import nl.kpmg.lcm.server.rest.authentication.ResponseFilter;
import nl.kpmg.lcm.server.rest.authentication.Roles;
import nl.kpmg.lcm.server.rest.authorization.AuthorizationRequestFilter;
import nl.kpmg.lcm.server.task.TaskManager;
import nl.kpmg.lcm.server.task.TaskManagerException;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.linking.DeclarativeLinkingFeature;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.message.filtering.SecurityAnnotations;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.lang.annotation.Annotation;

public class Server {

  private static final Logger LOGGER = LoggerFactory.getLogger(Server.class.getName());

  private final ServerConfiguration configuration;
  private final ApplicationContext context;

  private final String baseUri;
  private final String baseFallbackUri;

  private HttpsServerWrapper restServer;
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
        LOGGER.info( "Loading file based storage ApplicationContext");
        context = new ClassPathXmlApplicationContext(
            new String[] {"application-context-server-file.xml"});
        break;
      case "mongo":
        LOGGER.info( "Loading mongo based storage ApplicationContext");
        try {
        context = new ClassPathXmlApplicationContext(
            new String[] {"application-context-server-mongo.xml"});
        } catch (Exception e) {
            throw new ServerException("Unable to initialize monogo cotext! "
                    + "Check if Mongo DB is up and running and accessible.Error message:"  +  e.getMessage());
        }
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
  private HttpsServerWrapper startRestInterface() throws SslConfigurationException, IOException {
    // create a resource config that scans for JAX-RS resources and providers
    // in nl.kpmg.lcm.server.rest
    final ResourceConfig rc =
        new ResourceConfig().packages("nl.kpmg.lcm.server.rest").property("contextConfig", context)
            .property(EntityFilteringFeature.ENTITY_FILTERING_SCOPE,
                new Annotation[] {SecurityAnnotations
                    .rolesAllowed(new String[] {Roles.ADMINISTRATOR, Roles.API_USER})})
            .register(JacksonFeature.class).register(JacksonJsonProvider.class)
            .register(AuthenticationRequestFilter.class).register(AuthorizationRequestFilter.class)
            .register(ResponseFilter.class).register(DeclarativeLinkingFeature.class)
            .register(UriBuilderEntityProcessor.class).register(ValidationExceptionMapper.class)
            .register(LcmExceptionMapper.class).register(GeneralExceptionMapper.class);

    return HttpsServerProvider.createHttpsServer(configuration, baseUri, baseFallbackUri, rc, true);
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
      LcmIdService lcmIdService = context.getBean(LcmIdService.class);
      lcmIdService.create();
    } catch (SslConfigurationException ex) {
      LOGGER.error(
          "Failed starting the LocalCatalogManager due to invalid SSL configuration", ex);
      throw new ServerException(ex);
    } catch (IOException ex) {
      LOGGER.error(
          "Failed starting the LocalCatalogManager due to the redirect server ", ex);
      throw new ServerException(ex);
    } catch (TaskManagerException ex) {
      LOGGER.error(
          "Failed starting the LocalCatalogManager due to the TaskManager", ex);
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

  /**
   *
   * @return the HTTP URI of the server
   */
  public String getBaseFallbackUri() {
    return baseFallbackUri;
  }
}
