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

package nl.kpmg.lcm.common.client;

import nl.kpmg.lcm.common.SslConfigurationException;
import nl.kpmg.lcm.common.SslProvider;
import nl.kpmg.lcm.common.configuration.ClientConfiguration;
import nl.kpmg.lcm.common.ServerException;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

@Component
public class HttpsClientFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpsClientFactory.class.getName());

  private final ClientConfiguration configuration;
  private HttpAuthenticationFeature basicAutorization;


  public HttpsClientFactory(ClientConfiguration configuration, HttpAuthenticationFeature feature) {
    this.configuration = configuration;
    this.basicAutorization = feature;
  }

  @Autowired
  public HttpsClientFactory(ClientConfiguration configuration) {
    this.configuration = configuration;
  }

  private Client create() {
    SSLContext sc;
    ClientBuilder builder = ClientBuilder.newBuilder();
    try {
      sc = SslProvider.createSSLContextConfigurator(configuration).createSSLContext();
    } catch (SslConfigurationException ex) {

      LOGGER.warn(
          "Invalid SSL configuration, client will contact the configured server on HTTP only");
      return builder.build();
    }
    try {
      builder.hostnameVerifier(new javax.net.ssl.HostnameVerifier() {
        public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
          if (hostname.equals(configuration.getTargetHost())) {
            return true;
          }
          return false;
        }
      });
      builder = builder.sslContext(sc);
      if (basicAutorization != null) {
        builder.register(basicAutorization);
      }
    } catch (NullPointerException npe) {
      LOGGER.warn( "Null SSL context, skipping client SSL configuration", npe);
    }
    return builder.build();
  }

  public WebTarget createWebTarget(String targetURI) throws ServerException {
    try {
      Client client = create();
      WebTarget webTarget = client.target(targetURI);
      return webTarget;
    } catch (NullPointerException npe) {
      LOGGER.error( "Cannot create the web target, null target uri", npe);
      throw new ServerException(npe);
    } catch (IllegalArgumentException iae) {
      LOGGER.error( "Cannot create the web target, malformed target uri", iae);
      throw new ServerException(iae);
    }
  }
}
