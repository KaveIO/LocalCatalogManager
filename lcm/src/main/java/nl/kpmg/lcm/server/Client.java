package nl.kpmg.lcm.server;

public class Client extends nl.kpmg.lcm.AbstractHTTPSClient {

	public Client() {	       
        super.configuration = parentContext.getBean(Configuration.class);
    }
	
}
