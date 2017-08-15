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

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class HttpsServerProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpsServerProvider.class.getName());

  public static HttpsServerWrapper createHttpsServer(BasicConfiguration configuration,
      String baseUri, String baseFallbackUri, ResourceConfig rc, boolean clientAuth)
      throws SslConfigurationException, IOException {

    if (configuration.isUnsafe()) {
      LOGGER.warn(
          "Server configured as unsafe. Server will start on the non-secure port and run on HTTP only");

      HttpServer server =
          GrizzlyHttpServerFactory.createHttpServer(URI.create(baseFallbackUri), rc);
      initListeners(server.getListeners());
      return new HttpsServerWrapper(server);
    } else {
      try {
        SSLContextConfigurator sslContextConfigurator =
            SslProvider.createSSLContextConfigurator(configuration);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), rc, true,
            new SSLEngineConfigurator(sslContextConfigurator).setClientMode(false)
                .setNeedClientAuth(clientAuth));
        initListeners(server.getListeners());
        HttpServer redirectServer =
            HttpServer.createSimpleServer(null, configuration.getServicePort());
        initListeners(redirectServer.getListeners());
        redirectServer.getServerConfiguration().addHttpHandler(new HttpHandler() {
          @Override
          public void service(Request request, Response response) throws Exception {
            response.setStatus(HttpStatus.MOVED_PERMANENTLY_301);
            // Redirecting to the secure server
            response.setHeader(Header.Location, "https://" + configuration.getServiceName() + ":"
                + configuration.getSecureServicePort() + request.getRequestURI());
          }
        }, "");
        redirectServer.start();

        return new HttpsServerWrapper(server, redirectServer);
      } catch (SslConfigurationException ex) {
        LOGGER.error( "Invalid SSL configuration and unsafe mode off, abort");
        throw ex;
      }
    }
  }

  private static void initListeners(Collection<NetworkListener> listeners) {
    for (NetworkListener listener : listeners) {
      initNetworkListener(listener);
    }
  }

  private static void initNetworkListener(NetworkListener listener) {
    final TCPNIOTransport transport = listener.getTransport();
    transport.setKeepAlive(true);
    transport.setWriteTimeout(0, TimeUnit.MINUTES);
  }
}
