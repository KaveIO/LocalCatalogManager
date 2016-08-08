package nl.kpmg.lcm;

import nl.kpmg.lcm.configuration.BasicConfiguration;

import org.glassfish.grizzly.ssl.SSLContextConfigurator;

import nl.kpmg.lcm.server.ServerException;

public class SSLProvider {

  public static SSLContextConfigurator createSSLContextConfigurator(
      BasicConfiguration configuration) throws ServerException {

    SSLContextConfigurator sslContextConfigurator = new SSLContextConfigurator();


    // set up security context
    sslContextConfigurator.setKeyStoreFile(configuration.getKeystore()); // contains the server
                                                                         // keypair
    sslContextConfigurator.setKeyStorePass(configuration.getKeystorePassword());
    sslContextConfigurator.setKeyStoreType(configuration.getKeystoreType());
    sslContextConfigurator.setKeyPass(configuration.getKeystoreKeypass());
    sslContextConfigurator.setTrustStoreFile(configuration.getTruststore()); // contains the list of
                                                                             // trusted certificates
    sslContextConfigurator.setTrustStorePass(configuration.getTruststorePassword());
    sslContextConfigurator.setTrustStoreType(configuration.getTruststoreType());

    if (!sslContextConfigurator.validateConfiguration(true))
      throw new ServerException("Invalid SSL configuration");

    return sslContextConfigurator;

  }

}
