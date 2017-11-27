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

import nl.kpmg.lcm.common.data.metadata.DataDescriptor;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.validation.Notification;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shristov
 */
public class DataDescriptorTest {
  @Test
  public void testConstruction() {
    String uri = "csv://local2/mock.csv";
    List uriList = new ArrayList();
    uriList.add(uri);
    MetaData metaData = new MetaData();
    DataDescriptor data = new DataDescriptor(metaData);

    data.setUri(uriList);
    assertEquals(uriList, data.getUri());

    Map map = metaData.get(data.getSectionName());
    assertNotNull(map);
    assertEquals(1, map.size());
  }

  @Test
  public void testBlankConstruction() {

    MetaData metaData = new MetaData();
    DataDescriptor data = new DataDescriptor(metaData);

    assertNull(data.getUri());
  }

  @Test
  public void testValidate() {
    String uri = "csv://local2/mock.csv";
    List uriList = new ArrayList();
    uriList.add(uri);
    String path = "kpmg/lcm/test";

    MetaData metaData = new MetaData();
    DataDescriptor data = new DataDescriptor(metaData);

    data.setUri(uriList);
    data.setPath(path);

    Notification notification = new Notification();
    data.validate(notification);
    assertFalse(notification.hasErrors());
  }

  @Test
  public void testValidateNegative1() {

    MetaData metaData = new MetaData();
    DataDescriptor data = new DataDescriptor(metaData);

    Notification notification = new Notification();
    data.validate(notification);
    assertTrue(notification.hasErrors());
  }

  @Test
  public void testValidDataFormat() {
    MetaData metaData = new MetaData();

    String name = "mongoMetadata";
    metaData.setName(name);

    String uri = "mongo://mongoStorage/dir1/dir2/../table";
    List uriList = new ArrayList();
    uriList.add(uri);
    String path = "kpmg2";

    DataDescriptor data = new DataDescriptor(metaData);
    data.setUri(uriList);
    data.setPath(path);

    Notification notification = new Notification();
    data.validate(notification);
    assertFalse(notification.hasErrors());
  }


  @Test
  public void testValidDataItemExtension() {
    MetaData metaData = new MetaData();

    String name = "fileMetadata";
    metaData.setName(name);

    String uri = "file://fileStorage/file*.txt";
    List uriList = new ArrayList();
    uriList.add(uri);
    String path = "kpmg2";

    DataDescriptor data = new DataDescriptor(metaData);
    data.setUri(uriList);
    data.setPath(path);

    Notification notification = new Notification();
    data.validate(notification);
    assertFalse(notification.hasErrors());
  }

  @Test
  public void testInvalidDataFormat() {
    MetaData metaData = new MetaData();

    String name = "metadata";
    metaData.setName(name);

    String uri = "notExistingDataFormat://storage/dir1/dir2/../file";
    List uriList = new ArrayList();
    uriList.add(uri);
    String path = "kpmg2";

    DataDescriptor data = new DataDescriptor(metaData);
    data.setUri(uriList);
    data.setPath(path);

    Notification notification = new Notification();
    data.validate(notification);
    assertTrue(notification.hasErrors());
  }

  @Test
  public void testInvalidStorageName() {
    MetaData metaData = new MetaData();

    String name = "hiveMetadata";
    metaData.setName(name);

    String uri = "hive://hive Storage/dir1/dir2/../table";
    List uriList = new ArrayList();
    uriList.add(uri);
    String path = "kpmg2";

    DataDescriptor data = new DataDescriptor(metaData);
    data.setUri(uriList);
    data.setPath(path);

    Notification notification = new Notification();
    data.validate(notification);
    assertTrue(notification.hasErrors());
  }


  @Test
  public void testInvalidDataItemPath() {
    MetaData metaData = new MetaData();

    String name = "azurecsvMetadata";
    metaData.setName(name);

    String uri = "azurecsv://azureStorage/dir1$/dir2/../table";
    List uriList = new ArrayList();
    uriList.add(uri);
    String path = "kpmg2";

    DataDescriptor data = new DataDescriptor(metaData);
    data.setUri(uriList);
    data.setPath(path);

    Notification notification = new Notification();
    data.validate(notification);
    assertTrue(notification.hasErrors());
  }


  @Test
  public void testInvalidDataItemExtension() {
    MetaData metaData = new MetaData();

    String name = "hdfsFileMetadata";
    metaData.setName(name);

    String uri = "hdfsfile://hdfsFileStorage/file.txt*";
    List uriList = new ArrayList();
    uriList.add(uri);
    String path = "kpmg2";

    DataDescriptor data = new DataDescriptor(metaData);
    data.setUri(uriList);
    data.setPath(path);

    Notification notification = new Notification();
    data.validate(notification);
    assertTrue(notification.hasErrors());
  }

}
