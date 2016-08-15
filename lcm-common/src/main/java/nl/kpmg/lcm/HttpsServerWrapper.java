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

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * Wrapper class to tie the server and redirect server to each other.
 *
 * @author mhoekstra
 */
public class HttpsServerWrapper {
  private final HttpServer server;
  private HttpServer redirectServer;

  public HttpsServerWrapper(HttpServer server) {
    this.server = server;
  }

  public HttpsServerWrapper(HttpServer server, HttpServer redirectServer) {
    this.server = server;
    this.redirectServer = redirectServer;
  }

  public HttpServer getServer() {
    return server;
  }

  public HttpServer getRedirectServer() {
    return redirectServer;
  }

  public void stop() {
    if (server != null) {
      server.shutdownNow();
    }
    if (redirectServer != null) {
      redirectServer.shutdownNow();
    }
  }
}
