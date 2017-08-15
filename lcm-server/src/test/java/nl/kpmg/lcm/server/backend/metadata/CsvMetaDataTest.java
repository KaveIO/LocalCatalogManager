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

import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.server.test.mock.MetaDataMocker;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.util.FileHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shristov
 */
public class CsvMetaDataTest {
  @Test
  public void test() {
    MetaData metaData = MetaDataMocker.getMetaData();

    List columns = new ArrayList();
    columns.add(new ColumnDescription("name", ColumnType.STRING));
    columns.add(new ColumnDescription("age", ColumnType.STRING));
    String testKey = "1283245748395bvbx";
    TableDescriptionDescriptor tableDescription = new TableDescriptionDescriptor(metaData, testKey);
    tableDescription.setColumns(columns);
    metaData.set(tableDescription.getSectionName(), tableDescription.getMap());

    TableConfigurationDescriptor tableConfiguration = new TableConfigurationDescriptor(metaData, testKey);
    tableConfiguration.setEncoding(FileHelper.UTF_8_ENCODING);
    metaData.set(tableConfiguration.getSectionName(), tableConfiguration.getMap());

    CsvMetaData csvMetaData = new CsvMetaData(metaData);
    CsvConfiguration csvConfiguration = csvMetaData.getConfiguration(testKey);
    Assert.assertNotNull(csvConfiguration);
    Assert.assertEquals(FileHelper.UTF_8_ENCODING, csvConfiguration.getEncoding());
  }
}
