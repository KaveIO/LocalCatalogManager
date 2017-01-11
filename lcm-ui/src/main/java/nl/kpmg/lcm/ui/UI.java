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

package nl.kpmg.lcm.ui;

import com.vaadin.spring.server.SpringVaadinServlet;

import nl.kpmg.lcm.HttpsServerProvider;
import nl.kpmg.lcm.HttpsServerWrapper;
import nl.kpmg.lcm.SslConfigurationException;
import nl.kpmg.lcm.configuration.UiConfiguration;
import nl.kpmg.lcm.server.GeneralExceptionMapper;
import nl.kpmg.lcm.server.ServerException;

import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UI {
  private static final Logger LOGGER = Logger.getLogger(UI.class.getName());

  private final UiConfiguration configuration;
  private final ApplicationContext context;

  private final boolean unsafe;
  private final String baseUri;
  private final String unsafeUri;

  private HttpsServerWrapper uiServer;

  public UI() {
    context = new ClassPathXmlApplicationContext(new String[] {"application-context-ui.xml"});

    configuration = context.getBean(UiConfiguration.class);

    unsafe = configuration.isUnsafe();
    baseUri = String.format("https://%s:%s/", configuration.getServiceName(),
        configuration.getSecureServicePort());
    unsafeUri = String.format("http://%s:%s/", configuration.getServiceName(),
        configuration.getServicePort());
  }

  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   *
   * @return Grizzly HTTP server.
   */
  public HttpsServerWrapper startUserInterface() throws SslConfigurationException, IOException {
    // create a resource config that scans for JAX-RS resources and providers
    // in nl.kpmg.lcm.server.rest
    final ResourceConfig rc = new ResourceConfig().property("contextConfig", context)
        .registerClasses(GeneralExceptionMapper.class);

    XmlWebApplicationContext appContext = new XmlWebApplicationContext();
    appContext.setConfigLocation("application-context-ui.xml");

    // Grizzly ssl configuration
    HttpsServerWrapper grizzlyServer =
        HttpsServerProvider.createHttpsServer(configuration, baseUri, unsafeUri, rc, false);

    // Create default servlet and manual configuration
    WebappContext webappContext = new WebappContext("Grizzly web context", "/");
    webappContext.addListener(new ContextLoaderListener(appContext));
    ServletRegistration servlet = webappContext.addServlet("vaadin", SpringVaadinServlet.class);

    servlet.addMapping("/");
    servlet.addMapping("/*");
    servlet.addMapping("/VAADIN/*");

    servlet.setInitParameter("productionMode", "false");
    servlet.setLoadOnStartup(1);

    webappContext.deploy(grizzlyServer.getServer());

    return grizzlyServer;
  }

  /**
   * Main method.
   *
   * @throws ServerException
   */
  public void start() throws ServerException {
    try {
      uiServer = startUserInterface();
    } catch (SslConfigurationException ex) {
      Logger.getLogger(UI.class.getName()).log(Level.SEVERE,
          "Failed starting the LocalCatalogManager due to invalid SSL configuration", ex);
      throw new ServerException(ex);
    } catch (IOException ex) {
      Logger.getLogger(UI.class.getName()).log(Level.SEVERE,
          "Failed starting the LocalCatalogManager due to the redirect server ", ex);
      throw new ServerException(ex);
    }
  }

  public void stop() {
    uiServer.stop();
  }
}
