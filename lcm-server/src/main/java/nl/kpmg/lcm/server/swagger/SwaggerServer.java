
/*
 * Copyright 2017 KPMG N.V. (unless otherwise stated).
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

package nl.kpmg.lcm.server.swagger;

import nl.kpmg.lcm.common.GeneralExceptionMapper;
import nl.kpmg.lcm.server.Main;

import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.net.URI;

import io.swagger.jaxrs.config.BeanConfig;

public class SwaggerServer {

  private HttpServer swaggerServer;
 // open swaggerUri/docs  and place swaggerUri/swagger.json in the field
  public void initialize(String swaggerUri) {
    GenericWebApplicationContext context = new GenericWebApplicationContext();
    final ResourceConfig resourceConfig = new ResourceConfig();
    resourceConfig.property("contextConfig", context).register(GeneralExceptionMapper.class);
    resourceConfig.register(io.swagger.jaxrs.listing.ApiListingResource.class);
    resourceConfig.register(io.swagger.jaxrs.listing.SwaggerSerializers.class);

    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion("1.0.2");
    beanConfig.setSchemes(new String[] {"http"});
    beanConfig.setBasePath("/api");
    beanConfig.setResourcePackage("nl.kpmg.lcm.server.rest");
    beanConfig.setScan(true);

    swaggerServer =
        GrizzlyHttpServerFactory.createHttpServer(URI.create(swaggerUri), resourceConfig);

    ClassLoader loader = Main.class.getClassLoader();
    CLStaticHttpHandler docsHandler = new CLStaticHttpHandler(loader, "swagger-ui/");
    docsHandler.setFileCacheEnabled(false);

    ServerConfiguration cfg = swaggerServer.getServerConfiguration();
    cfg.addHttpHandler(docsHandler, "/docs/");
  }

  public void stop() {
    swaggerServer.shutdownNow();
  }
}