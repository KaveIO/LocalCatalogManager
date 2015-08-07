/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.kpmg.lcm.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Singleton;
import javax.ws.rs.core.Link;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson JSON processor could be controlled via providing a custom Jackson ObjectMapper instance.
 * This could be handy if you need to redefine the default Jackson behavior and to fine-tune how
 * your JSON data structures look like (copied from Jersey web site). *
 * @see https://jersey.java.net/documentation/latest/media.html#d0e4799
 *
 *
 * @author mhoekstra
 */
@Provider
@Singleton
public class JacksonJsonProvider implements ContextResolver<ObjectMapper> {
    /**
     * The Logger of this class.
     */
    private static final Logger LOGGER = Logger.getLogger(JacksonJsonProvider.class.getName());

    /**
     * The actual Mapper used for serializing and un-serializing of objects.
     *
     * This is completely static.
     *
     * We enable inclusion of any NON_EMPTY json field on serialization mainly
     * so we can have somewhat clean json arriving at the client side.
     *
     * We disable FAIL_ON_UNKNOWN_PROPERTIES for the MetaData class. We try to
     * safeguard freedom for any MetaData usage scenario. Therefor we allow fields
     * to be persisted we don't know yet.
     *
     * Added a custom Link serializer to facilitate pretty HATEOAS links.
     */
    @SuppressWarnings("serial")
	private static final ObjectMapper MAPPER = new ObjectMapper() { {
        setSerializationInclusion(Include.NON_EMPTY);
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        // Add the linkSerializer. Used for pretty printing of hyperlinks in restful media
        SimpleModule linkSerializer = new SimpleModule();
        linkSerializer.addSerializer(Link.class, new LinkSerializer());
        registerModule(linkSerializer);
        setFilters(new NotFilteringFilterProvider());
    } };

    /**
     * Default Constructor.
     *
     * Logs its usage for clarity other wise irrelevant.
     */
    public JacksonJsonProvider() {
        LOGGER.log(Level.INFO, "Instantiate MyJacksonJsonProvider");
    }

    /**
     * Returns the requested ObjectMapper.
     *
     * Since we only have one static ObjectMapper the response is rather simple.
     *
     * @param type the class for which a mapper is requested
     * @return the ObjectMapper
     */
    @Override
    public final ObjectMapper getContext(final Class<?> type) {
        LOGGER.log(Level.INFO, "MyJacksonProvider.getContext() called with type: {0}", type);
        return MAPPER;
    }

    /**
     * Inner static class for serializing Link objects.
     */
    public static class LinkSerializer extends JsonSerializer<Link> {

        @Override
        public final void serialize(final Link link, final JsonGenerator jg, final SerializerProvider sp)
                throws IOException {

            jg.writeStartObject();
            jg.writeStringField("rel", link.getRel());
            jg.writeStringField("href", link.getUri().toString());
            if (link.getType() != null) {
                jg.writeStringField("type", link.getType());
            }
            jg.writeEndObject();
        }
    }
}

