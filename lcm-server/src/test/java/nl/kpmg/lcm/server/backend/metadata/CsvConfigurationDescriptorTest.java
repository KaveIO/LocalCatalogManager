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
package nl.kpmg.lcm.server.backend.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.validation.Notification;

import org.apache.metamodel.util.FileHelper;
import org.junit.Test;

import java.util.Map;

/**
 *
 * @author shristov
 */
public class CsvConfigurationDescriptorTest {
  @Test
  public void testConstruction() {
    MetaData metaData = new MetaData();
    String testKey = "1283245748395bvbx";
    CsvConfigurationDescriptor csvConfiguration = new CsvConfigurationDescriptor(metaData, testKey);
    Character escapeChar = '\\';
    Character quoteChar = '"';
    Character separatorChar = ',';
    String encoding = FileHelper.UTF_8_ENCODING;
    Integer columnNameLine = 1;

    csvConfiguration.setColumnNameLine(columnNameLine);
    csvConfiguration.setEscapeChar(escapeChar);
    csvConfiguration.setQuoteChar(quoteChar);
    csvConfiguration.setSeparatorChar(separatorChar);
    csvConfiguration.setEncoding(encoding);
    assertEquals(csvConfiguration.getEncoding(), encoding);
    assertEquals(csvConfiguration.getColumnNameLine(), columnNameLine);
    assertEquals(csvConfiguration.getEscapeChar(), escapeChar);
    assertEquals(csvConfiguration.getSeparatorChar(), separatorChar);
    assertEquals(csvConfiguration.getQuoteChar(), quoteChar);

    metaData.set(csvConfiguration.getSectionName(), csvConfiguration.getMap());
    Map map = metaData.get(csvConfiguration.getSectionName());
    assertNotNull(map);
    assertEquals(5, map.size());
  }

  @Test
  public void testBlankConstruction() {

    MetaData metaData = new MetaData();
    String testKey = "1283245748395bvbx";
    CsvConfigurationDescriptor csvConfiguration = new CsvConfigurationDescriptor(metaData, testKey);

    assertNull(csvConfiguration.getEncoding());
    assertNull(csvConfiguration.getColumnNameLine());
    assertNull(csvConfiguration.getEscapeChar());
    assertNull(csvConfiguration.getQuoteChar());
    assertNull(csvConfiguration.getSeparatorChar());
  }

  @Test
  public void testValidate() {
    MetaData metaData = new MetaData();
    String testKey = "1283245748395bvbx";
    CsvConfigurationDescriptor csvConfiguration = new CsvConfigurationDescriptor(metaData, testKey);
    csvConfiguration.setColumnNameLine(1);
    csvConfiguration.setEscapeChar('\\');
    csvConfiguration.setQuoteChar('"');
    csvConfiguration.setSeparatorChar(',');
    csvConfiguration.setEncoding(FileHelper.UTF_8_ENCODING);

    Notification notification = new Notification();
    csvConfiguration.validate(notification);
    assertFalse(notification.hasErrors());
  }
}
