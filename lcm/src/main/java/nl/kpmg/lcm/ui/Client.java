package nl.kpmg.lcm.ui;

import nl.kpmg.lcm.client.Configuration;

public class Client extends nl.kpmg.lcm.AbstractHTTPSClient {

  public Client() {
    super.configuration = parentContext.getBean(Configuration.class);
  }

}
