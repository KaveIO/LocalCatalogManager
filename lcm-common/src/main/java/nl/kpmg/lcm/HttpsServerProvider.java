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

package nl.kpmg.lcm;

import nl.kpmg.lcm.configuration.BasicConfiguration;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpsServerProvider {

  private static final Logger LOGGER = Logger.getLogger(HttpsServerProvider.class.getName());

  public static HttpsServerWrapper createHttpsServer(BasicConfiguration configuration,
      String baseUri, String baseFallbackUri, ResourceConfig rc, boolean clientAuth)
      throws SslConfigurationException, IOException {

    if (configuration.isUnsafe()) {
      LOGGER.log(Level.WARNING,
          "Server configured as unsafe. Server will start on the non-secure port and run on HTTP only");

      HttpServer server =
          GrizzlyHttpServerFactory.createHttpServer(URI.create(baseFallbackUri), rc);

      return new HttpsServerWrapper(server);
    } else {
      try {
        SSLContextConfigurator sslContextConfigurator =
            SslProvider.createSSLContextConfigurator(configuration);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), rc, true,
            new SSLEngineConfigurator(sslContextConfigurator).setClientMode(false)
                .setNeedClientAuth(clientAuth));

        HttpServer redirectServer =
            HttpServer.createSimpleServer(null, configuration.getServicePort());
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
        LOGGER.log(Level.SEVERE, "Invalid SSL configuration and unsafe mode off, abort");
        throw ex;
      }
    }
  }
}
