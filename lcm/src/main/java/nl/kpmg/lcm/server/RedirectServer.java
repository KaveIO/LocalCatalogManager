package nl.kpmg.lcm.server;

import nl.kpmg.lcm.AbstractRedirectServer;

public class RedirectServer extends AbstractRedirectServer {
	
	public RedirectServer() {	       
        super.configuration = parentContext.getBean(Configuration.class);
    }
	
}
