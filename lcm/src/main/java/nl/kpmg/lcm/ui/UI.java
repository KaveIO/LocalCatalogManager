package nl.kpmg.lcm.ui;

import nl.kpmg.lcm.HTTPServerProvider;
import nl.kpmg.lcm.SSLProvider;
import nl.kpmg.lcm.client.Configuration;
import nl.kpmg.lcm.server.*;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.kpmg.lcm.server.task.TaskManager;
import com.vaadin.server.VaadinServlet;
import javax.servlet.ServletConfig;
import org.glassfish.grizzly.servlet.ServletHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.ServletConfigImpl;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.xpoft.vaadin.SpringApplicationContext;

/**
 *
 * @author mhoekstra
 */
public class UI {
    private static final Logger LOGGER = Logger.getLogger(UI.class.getName());

    private Configuration configuration;
    private final ApplicationContext context;

    private final String baseUri;
    private final String baseFallbackUri;

    private HttpServer restServer;
    private TaskManager taskManager;

    public UI() {
        context = new ClassPathXmlApplicationContext(new String[] {
            "application-context-ui.xml"
        });

        configuration = context.getBean(Configuration.class);
        baseUri = String.format(
                "https://%s:%s/",
                configuration.getServiceName(),
                configuration.getSecureServicePort());
        baseFallbackUri = String.format(
                "http://%s:%s/",
                configuration.getServiceName(),
                configuration.getServicePort());
    }

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public HttpServer startUserInterface() throws ServerException {
        // create a resource config that scans for JAX-RS resources and providers
        // in nl.kpmg.lcm.server.rest
        final ResourceConfig rc = new ResourceConfig()
            .property("contextConfig", context)
            .registerClasses(LoggingExceptionMapper.class);

        // Grizzly ssl configuration
        HttpServer grizzlyServer = HTTPServerProvider.createHTTPServer(configuration, baseUri, baseFallbackUri, rc, false);

        // Method A: way more elegant however breaks down
        // WebappContext webappContext = new WebappContext("Grizzly web context", "");
        // ServletRegistration servlet = webappContext.addServlet("vaadin", Servlet.class);
        // webappContext.deploy(grizzlyServer);

        // Method B: via default servlet and manual configuration
        WebappContext webappContext = new WebappContext("Grizzly web context", "");
        ServletRegistration servlet = webappContext.addServlet("vaadin", ru.xpoft.vaadin.SpringVaadinServlet.class);
        SpringApplicationContext.setApplicationContext(context);

        servlet.addMapping("");
        servlet.addMapping("/*");
        servlet.addMapping("/VAADIN/*");

        servlet.setInitParameter("UI", "nl.kpmg.lcm.ui.Application");
        servlet.setInitParameter("productionMode", "false");
        servlet.setLoadOnStartup(1);

        webappContext.deploy(grizzlyServer);

        return grizzlyServer;
    }

    /**
     * Main method.
     * @throws ServerException
     */
    public void start() throws ServerException  {
        restServer = startUserInterface();
    }

    public void stop() {
        restServer.shutdownNow();
    }
}
