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
package nl.kpmg.lcm.server.data.dao.file;

import java.util.logging.Logger;


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Factory for creating ObjectMappers used for the file storage.
 *
 * @author mhoekstra
 */
public final class ObjectMapperFactory {

    /**
     * The Logger of this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ObjectMapperFactory.class.getName());

    /**
     * Creates a ObjectMapper instance.
     *
     * We enable inclusion of any NON_EMPTY json field on serialization mainly
     * so we can have somewhat clean json arriving at the client side.
     *
     * We disable FAIL_ON_UNKNOWN_PROPERTIES for the MetaData class. We try to
     * safeguard freedom for any MetaData usage scenario. Therefor we allow
     * fields to be persisted we don't know yet.
     *
     * @return the created mapper
     */
    public static ObjectMapper createInstance() {
        return new ObjectMapper() {
            {
                setSerializationInclusion(Include.NON_EMPTY);
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            }
        };
    }
}
