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

package nl.kpmg.lcm.common;

import nl.kpmg.lcm.common.configuration.BasicConfiguration;

import org.glassfish.grizzly.ssl.SSLContextConfigurator;

public class SslProvider {

  public static SSLContextConfigurator createSSLContextConfigurator(
      BasicConfiguration configuration) throws SslConfigurationException {

    SSLContextConfigurator sslContextConfigurator = new SSLContextConfigurator();

    // set up security context
    // contains the server keypair
    sslContextConfigurator.setKeyStoreFile(configuration.getKeystore());
    sslContextConfigurator.setKeyStorePass(configuration.getKeystorePassword());
    sslContextConfigurator.setKeyStoreType(configuration.getKeystoreType());
    sslContextConfigurator.setKeyPass(configuration.getKeystoreKeypass());
    // contains the list of trusted certificates
    sslContextConfigurator.setTrustStoreFile(configuration.getTruststore());
    sslContextConfigurator.setTrustStorePass(configuration.getTruststorePassword());
    sslContextConfigurator.setTrustStoreType(configuration.getTruststoreType());

    if (!sslContextConfigurator.validateConfiguration(true)) {
      throw new SslConfigurationException("Invalid SSL configuration");
    }

    return sslContextConfigurator;
  }
}
