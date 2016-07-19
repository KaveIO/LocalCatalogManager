package nl.kpmg.lcm.ui;

import nl.kpmg.lcm.AbstractRedirectServer;
import nl.kpmg.lcm.client.Configuration;

public class RedirectServer extends AbstractRedirectServer {
	
	public RedirectServer() {	       
        super.configuration = parentContext.getBean(Configuration.class);
    }
	
}
