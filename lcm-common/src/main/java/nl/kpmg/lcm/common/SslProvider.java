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

import nl.kpmg.lcm.common.configuration.ClientConfiguration;
import nl.kpmg.lcm.common.configuration.ServerConfiguration;

import org.glassfish.grizzly.ssl.SSLContextConfigurator;

public class SslProvider {

  public static SSLContextConfigurator createSSLServerContextConfigurator(
      ServerConfiguration configuration) throws SslConfigurationException {

    SSLContextConfigurator sslContextConfigurator = new SSLContextConfigurator(true);

    sslContextConfigurator.setKeyStoreFile(configuration.getKeystore());
    sslContextConfigurator.setKeyStorePass(configuration.getKeystorePassword());

    return sslContextConfigurator;
  }

  public static SSLContextConfigurator createSSLClientContextConfigurator(
      ClientConfiguration configuration) throws SslConfigurationException {

    SSLContextConfigurator sslContextConfigurator = new SSLContextConfigurator(true);

    sslContextConfigurator.setTrustStoreFile(configuration.getTruststore());

    return sslContextConfigurator;
  }
}
