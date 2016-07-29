package nl.kpmg.lcm;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import nl.kpmg.lcm.server.ServerException;

public abstract class AbstractHTTPSClient {
    private static final Logger LOGGER = Logger.getLogger(AbstractHTTPSClient.class.getName());

    protected BasicConfiguration configuration;
    
    private javax.ws.rs.client.Client client;
    private WebTarget webTarget;

    protected ApplicationContext parentContext = new ClassPathXmlApplicationContext(new String[] { "application-context.xml" });

	private Client create() {
		SSLContext sc;
		ClientBuilder builder = ClientBuilder.newBuilder();
		try { sc = SSLProvider.createSSLContextConfigurator(configuration).createSSLContext(); } catch (ServerException se) {
            LOGGER.log(Level.WARNING, "Invalid SSL configuration, client will contact the configured server on HTTP only");
            return builder.build();
		}
        try { builder = builder.sslContext(sc); } catch (NullPointerException npe) {
			LOGGER.log(Level.WARNING, "Null SSL context, skipping client SSL configuration", npe);
        }
        return builder.build();
	}
	
	public WebTarget createWebTarget(String targetURI) throws ServerException {
		try { 
			client = create();
			webTarget = client.target(targetURI);
		} catch (NullPointerException npe) {
			LOGGER.log(Level.SEVERE, "Cannot create the web target, null target uri", npe);
            throw new ServerException(npe);
		} catch (IllegalArgumentException iae) {
			LOGGER.log(Level.SEVERE, "Cannot create the web target, malformed target uri", iae);
            throw new ServerException(iae);
		}
		return webTarget;
	}
	
}

