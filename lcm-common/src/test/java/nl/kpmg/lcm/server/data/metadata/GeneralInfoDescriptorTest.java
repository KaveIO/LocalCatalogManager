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

import nl.kpmg.lcm.common.data.metadata.GeneralInfoDescriptor;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.validation.Notification;

import org.junit.Test;

import java.util.Map;

/**
 *
 * @author shristov
 */
public class GeneralInfoDescriptorTest {

  @Test
  public void testConstruction() {
    String owner = "KPMG";
    String description = "This is test metadata";

    MetaData metaData = new MetaData();
    GeneralInfoDescriptor generalInfo = new GeneralInfoDescriptor(metaData);

    generalInfo.setOwner(owner);
    generalInfo.setDescription(description);
    assertEquals(owner, generalInfo.getOwner());
    assertEquals(description, generalInfo.getDescription());

    Map map = metaData.get(generalInfo.getSectionName());
    assertNotNull(map);
    assertEquals(2, map.size());
  }

  @Test
  public void testBlankConstruction() {

    MetaData metaData = new MetaData();
    GeneralInfoDescriptor generalInfo = new GeneralInfoDescriptor(metaData);

    assertNull(generalInfo.getOwner());
    assertNull(generalInfo.getDescription());
  }

  @Test
  public void testValidate() {
    String owner = "KPMG";
    String description = "This is test metadata description";

    MetaData metaData = new MetaData();
    GeneralInfoDescriptor generalInfo = new GeneralInfoDescriptor(metaData);

    generalInfo.setOwner(owner);
    generalInfo.setDescription(description);
    Notification notification = new Notification();
    generalInfo.validate(notification);
    assertFalse(notification.hasErrors());
  }

  @Test
  public void testValidateInvalidOwner() {

    MetaData metaData = new MetaData();
    GeneralInfoDescriptor generalInfo = new GeneralInfoDescriptor(metaData);

    metaData.set(generalInfo.getSectionName() + ".owner", true);

    Notification notification = new Notification();
    generalInfo.validate(notification);
    assertTrue(notification.hasErrors());
  }

  @Test
  public void testValidateInvalidOwner2() {

    MetaData metaData = new MetaData();
    GeneralInfoDescriptor generalInfo = new GeneralInfoDescriptor(metaData);

    metaData.set(generalInfo.getSectionName() + ".owner", 1234);

    Notification notification = new Notification();
    generalInfo.validate(notification);
    assertTrue(notification.hasErrors());
  }

  @Test
  public void testValidateInvalidDescription() {

    MetaData metaData = new MetaData();
    GeneralInfoDescriptor generalInfo = new GeneralInfoDescriptor(metaData);

    metaData.set(generalInfo.getSectionName() + ".description", 1234);

    Notification notification = new Notification();
    generalInfo.validate(notification);
    assertTrue(notification.hasErrors());
  }

  @Test
  public void testValidateInvalidDescription2() {

    MetaData metaData = new MetaData();
    GeneralInfoDescriptor generalInfo = new GeneralInfoDescriptor(metaData);

    metaData.set(generalInfo.getSectionName() + ".description", 15.6f);

    Notification notification = new Notification();
    generalInfo.validate(notification);
    assertTrue(notification.hasErrors());
  }
}
