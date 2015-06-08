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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ejb.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

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
//@Produces({MediaType.APPLICATION_JSON})
//@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class JacksonJsonProvider implements ContextResolver<ObjectMapper> {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    static {
      MAPPER.setSerializationInclusion(Include.NON_EMPTY);
      MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      //MAPPER.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }
 
    public JacksonJsonProvider() {
        System.out.println("Instantiate MyJacksonJsonProvider");
    }
     
    @Override
    public ObjectMapper getContext(Class<?> type) {
        System.out.println("MyJacksonProvider.getContext() called with type: "+type);
        return MAPPER;
    } 
}