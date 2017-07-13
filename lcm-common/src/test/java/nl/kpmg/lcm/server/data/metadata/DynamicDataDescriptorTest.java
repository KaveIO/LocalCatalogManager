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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import nl.kpmg.lcm.validation.Notification;

import org.junit.Test;

import java.util.Map;

/**
 *
 * @author shristov
 */
public class DynamicDataDescriptorTest {
  @Test
  public void testConstruction() {
    Long sizeValue = 1234567L;
    String stateValue = "ATTACHED";
    Long timestamp = System.currentTimeMillis();

    MetaDataWrapper metaData = new MetaDataWrapper();

    String testKey = "test1234";

    DataItemsDescriptor dynamicDataDescriptor =
        new DataItemsDescriptor(metaData.getMetaData(), testKey);
    dynamicDataDescriptor.getDetailsDescriptor().setSize(sizeValue);
    dynamicDataDescriptor.getDetailsDescriptor().setState(stateValue);
    dynamicDataDescriptor.getDetailsDescriptor().setDataUpdateTimestamp(timestamp);
    metaData.getDynamicData().addDynamicDataDescriptors(testKey, dynamicDataDescriptor.getMap());

    Map dynamicDataMap = metaData.getDynamicData().getMap();
    assertNotNull(dynamicDataMap);

    DataItemsDescriptor readDynamicDataDescriptor =
        metaData.getDynamicData().getDynamicDataDescriptor(testKey);
    assertEquals(3, readDynamicDataDescriptor.getDetailsDescriptor().getMap().size());

    assertEquals(sizeValue, readDynamicDataDescriptor.getDetailsDescriptor().getSize());
    assertEquals(stateValue, readDynamicDataDescriptor.getDetailsDescriptor().getState());
    Long readTime = readDynamicDataDescriptor.getDetailsDescriptor().getDataUpdateTimestamp();
    assertEquals(timestamp, readTime);
  }

  @Test
  public void testBlankConstruction() {

    MetaDataWrapper metaData = new MetaDataWrapper();

    String testKey = "test1234";
    DataItemsDescriptor dynamicDataDescriptor =
        new DataItemsDescriptor(metaData.getMetaData(), testKey);
    assertNull(metaData.getDynamicData().getDynamicDataDescriptor(testKey));
    String testURI = "csv://local/test.csv";
    dynamicDataDescriptor.setURI(testURI);
    metaData.getDynamicData().addDynamicDataDescriptors(testKey, dynamicDataDescriptor.getMap());
    DataDetailsDescriptor details =
        metaData.getDynamicData().getDynamicDataDescriptor(testKey).getDetailsDescriptor();
    assertNull(details.getSize());
    assertNull(details.getState());
    assertNull(details.getDataUpdateTimestamp());
  }

  @Test
  public void testValidate() {
    Long sizeValue = 1234567L;
    String stateValue = "ATTACHED";
    Long timestamp = System.currentTimeMillis();

    MetaDataWrapper metaData = new MetaDataWrapper();

    String testKey = "test1234";
    DataItemsDescriptor dynamicDataDescriptor =
        new DataItemsDescriptor(metaData.getMetaData(), testKey);
    dynamicDataDescriptor.getDetailsDescriptor().setSize(sizeValue);
    dynamicDataDescriptor.getDetailsDescriptor().setState(stateValue);
    dynamicDataDescriptor.getDetailsDescriptor().setDataUpdateTimestamp(timestamp);
    metaData.getDynamicData().addDynamicDataDescriptors(testKey, dynamicDataDescriptor.getMap());

    Notification notification = new Notification();
    DataDetailsDescriptor details =
        metaData.getDynamicData().getDynamicDataDescriptor(testKey).getDetailsDescriptor();
    details.validate(notification);
    assertFalse(notification.hasErrors());
  }

  @Test
  public void testValidateNegative1() {

    MetaData metaData = new MetaData();
    DynamicDataDescriptor dynamicData = new DynamicDataDescriptor(metaData);

    String testKey = "test1234";
    assertNull(dynamicData.getDynamicDataDescriptor(testKey));
  }
}