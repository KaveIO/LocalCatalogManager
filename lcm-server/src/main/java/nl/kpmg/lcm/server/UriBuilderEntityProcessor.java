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

import org.glassfish.jersey.message.filtering.spi.AbstractEntityProcessor;
import org.glassfish.jersey.message.filtering.spi.EntityGraph;

import java.lang.annotation.Annotation;

import javax.ws.rs.core.UriBuilder;

/**
 * Entity processor to fix an issue created by the SecurityEntityFilteringFeature.
 *
 * We use the SecurityEntityFilteringFeature for role based access control. This works
 * fine by itself. However in combination with DeclarativeLinkingFeature this breaks
 * down.
 *
 * The DeclarativeLinkingFeature makes it possible to have nice Declarative links within
 * entity classes. This is used to build our HATEOAS API. Internally the links are
 * being generated through a UriBuilder. This class is somehow not added perfectly via
 * the SecurityEntityFilteringFeature in the object graph. This class corrects for this
 * effect.
 *
 * Honestly I'm not 100% sure of all the mechanics involved. This seems to be the correct
 * way to go, however this is very complex. What I think happens is that the a security
 * annotation is expected on the UriBuilder (in JerseyLink) which obviously isn't present
 * and since everything is declaratively there is not a way around that.
 *
 * The only way to really fix this is to debug Jersey. I suspect it really is a bug in
 * their code. However this cost a lot of time and is not worth it now.
 *
 * @author mhoekstra
 */
public class UriBuilderEntityProcessor extends AbstractEntityProcessor {

    @Override
    protected final Result process(final String fieldName, final Class<?> fieldClass,
            final Annotation[] fieldAnnotations, final Annotation[] annotations,
            final EntityGraph graph) {

        if (fieldClass != null && fieldClass.equals(UriBuilder.class)) {
            graph.addField(fieldName);
            return Result.APPLY;
        }

        return Result.SKIP;
    }
}
