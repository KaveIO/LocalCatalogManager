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
package nl.kpmg.lcm.server;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import nl.kpmg.lcm.server.rest.authentication.RequestFilter;
import nl.kpmg.lcm.server.rest.authentication.ResponseFilter;
import nl.kpmg.lcm.server.rest.authentication.Roles;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.linking.DeclarativeLinkingFeature;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.message.filtering.SecurityAnnotations;
import org.glassfish.jersey.server.ResourceConfig;

import java.lang.annotation.Annotation;

/**
 *
 * @author shristov
 */
public class LCMResource extends ResourceConfig {

    public LCMResource(){
            packages("nl.kpmg.lcm.server.rest")
            .property(EntityFilteringFeature.ENTITY_FILTERING_SCOPE,
                    new Annotation[]{SecurityAnnotations
                      .rolesAllowed(new String[]{Roles.ADMINISTRATOR, Roles.API_USER})})
            .register(JacksonFeature.class)
            .register(JacksonJsonProvider.class)
            .register(RequestFilter.class)
            .register(ResponseFilter.class)
            .register(DeclarativeLinkingFeature.class)
            .register(UriBuilderEntityProcessor.class)
            .register(LoggingExceptionMapper.class);
    }

}
