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
package nl.kpmg.lcm.server.data;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.kpmg.lcm.server.data.meatadata.MetaDataWrapper;

import org.junit.Test;

/**
 *
 * @author shristov
 */
public class MetaDataWrapperTest {
    @Test
  public void testJacksonObjectMapperDoesntIncludeUnusedFields() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();

    MetaDataWrapper metaDataWrapper = new MetaDataWrapper();

    metaDataWrapper.setName("test");
    metaDataWrapper.setSourceType("csv");
    metaDataWrapper.setDataUri("file://local/bla.csv");

    String expected = "{\"id\":null,\"name\":\"test\",\"sourceType\":\"csv\",\"data\":{\"uri\":\"file://local/bla.csv\"}}";
    String actual = objectMapper.writeValueAsString(metaDataWrapper.getMetaData());
    assertEquals(expected, actual);
  }
}
