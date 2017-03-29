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
import static org.junit.Assert.assertTrue;

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

    MetaData metaData = new MetaData();
    DynamicDataDescriptor dynamicData = new DynamicDataDescriptor(metaData);

    dynamicData.setSize(sizeValue);
    dynamicData.setState(stateValue);
    dynamicData.setDataUpdateTimestamp(timestamp);

    Map dynamicDataMap = metaData.get(dynamicData.getSectionName());
    assertNotNull(dynamicDataMap);
    assertEquals(3, dynamicDataMap.size());

    assertEquals(sizeValue, dynamicData.getSize());
    assertEquals(stateValue, dynamicData.getState());
    Long readTime = dynamicData.getDataUpdateTimestamp();
    assertEquals(timestamp, readTime);
  }

  @Test
  public void testBlankConstruction() {

    MetaData metaData = new MetaData();
    DynamicDataDescriptor dynamicData = new DynamicDataDescriptor(metaData);

    assertNull(dynamicData.getSize());
    assertNull(dynamicData.getState());
    assertNull(dynamicData.getDataUpdateTimestamp());
  }

  @Test
  public void testValidate() {
    Long sizeValue = 1234567L;
    String stateValue = "ATTACHED";
    Long timestamp = System.currentTimeMillis();

    MetaData metaData = new MetaData();
    DynamicDataDescriptor dynamicData = new DynamicDataDescriptor(metaData);

    dynamicData.setSize(sizeValue);
    dynamicData.setState(stateValue);
    dynamicData.setDataUpdateTimestamp(timestamp);

    Notification notification = new Notification();
    dynamicData.validate(notification);
    assertFalse(notification.hasErrors());
  }

  @Test
  public void testValidateNegative1() {

    MetaData metaData = new MetaData();
    DynamicDataDescriptor dynamicData = new DynamicDataDescriptor(metaData);

    Notification notification = new Notification();
    dynamicData.validate(notification);
    assertTrue(notification.hasErrors());
  }
}
