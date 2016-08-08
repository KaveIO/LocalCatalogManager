package nl.kpmg.lcm.ui;

import org.glassfish.grizzly.servlet.WebappContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionCookieConfig;

/**
 * @author mhoekstra
 */
public class CookieListener implements ServletContextListener {

  public CookieListener() {}

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    WebappContext servletContext = (WebappContext) servletContextEvent.getServletContext();
    SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
    // sessionCookieConfig.setSecure(true);
    sessionCookieConfig.setHttpOnly(false);
    sessionCookieConfig.setName("JSESSIONID");
    sessionCookieConfig.setDomain("localhost");
    sessionCookieConfig.setPath("/");
    sessionCookieConfig.setMaxAge(-1);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {}
}
