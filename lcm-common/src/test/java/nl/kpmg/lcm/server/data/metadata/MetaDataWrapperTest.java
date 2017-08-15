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
package nl.kpmg.lcm.server.data.metadata;

import nl.kpmg.lcm.common.data.metadata.DataDetailsDescriptor;
import nl.kpmg.lcm.common.data.metadata.DataDescriptor;
import nl.kpmg.lcm.common.data.metadata.GeneralInfoDescriptor;
import nl.kpmg.lcm.common.data.metadata.DynamicDataDescriptor;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.common.data.metadata.MetaData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

    String expected = "{\"name\":\"test\"}";
    String actual = objectMapper.writeValueAsString(metaDataWrapper.getMetaData().getInnerMap());
    assertEquals(expected, actual);
  }

  @Test
  @Ignore
  public void createWarapperFromMetaData() {
    MetaData metaData = new MetaData();

    metaData.setId("4738596435");
    metaData.setName("Test");
    DynamicDataDescriptor dynamicData = new DynamicDataDescriptor(metaData);
    String testKey = "test1234";
    DataDetailsDescriptor details = dynamicData.getDynamicDataDescriptor(testKey).getDetailsDescriptor();
    details.setSize(123L);
    details.setState("ATTACHED");
    details.setDataUpdateTimestamp(12423423L);

    metaData.set(dynamicData.getSectionName() + testKey, dynamicData.getDynamicDataDescriptor(testKey).getMap());

    DataDescriptor data = new DataDescriptor(metaData);
    String uri = "csv://local2/mock.csv";
    List uriList = new ArrayList();
    uriList.add(uri);
    data.setUri(uriList);
    data.setPath("kpmg/lcm/test");

    metaData.set(data.getSectionName(), data.getMap());

    GeneralInfoDescriptor generalInfo = new GeneralInfoDescriptor(metaData);
    generalInfo.setOwner("KPMG");
    generalInfo.setDescription("Test MetaDataWrapper");

    metaData.set(generalInfo.getSectionName(), generalInfo.getMap());

    MetaDataWrapper wrapper = new MetaDataWrapper(metaData);
    assertNotNull(wrapper.getDynamicData());
    assertNotNull(wrapper.getGeneralInfo());
    assertNotNull(wrapper.getData());
    assertEquals(wrapper.getId(), wrapper.getId());
    assertEquals(wrapper.getName(), metaData.getName());
    assertEquals(wrapper.getData().getMap(), data.getMap());
    assertEquals(wrapper.getDynamicData().getDynamicDataDescriptor(testKey).getMap(), dynamicData.getDynamicDataDescriptor(testKey).getMap());
    assertEquals(wrapper.getGeneralInfo().getMap(), generalInfo.getMap());
    assertEquals(wrapper.getData().getStorageItemName().get(0), "/mock.csv");
  }
}
