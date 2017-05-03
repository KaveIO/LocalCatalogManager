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
package nl.kpmg.lcm.server.test.mock;

import nl.kpmg.lcm.server.backend.metadata.ColumnDescription;
import nl.kpmg.lcm.server.backend.metadata.CsvMetaData;
import nl.kpmg.lcm.server.backend.metadata.TabularMetaData;
import nl.kpmg.lcm.server.data.metadata.DataDescriptor;
import nl.kpmg.lcm.server.data.metadata.GeneralInfoDescriptor;
import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.server.data.metadata.MetaDataWrapper;

import org.apache.metamodel.schema.ColumnType;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shristov
 */
public class MetaDataMocker {
  public static MetaDataWrapper getCsvMetaDataWrapper() {
    CsvMetaData csvMetaData = new CsvMetaData();
    csvMetaData.setName("CSV-metadata");
    csvMetaData.setId("585a57136d31212d0ad5fca6");
    csvMetaData.getData().setUri("csv://test/temp.csv");
    csvMetaData.getData().setPath("kpmg/lcm/test");
    csvMetaData.getGeneralInfo().setOwner("KPMG");
    csvMetaData.getGeneralInfo().setDescription("Sample description");
    List columns = new ArrayList();
    columns.add(new ColumnDescription("name", ColumnType.STRING));
    columns.add(new ColumnDescription("age", ColumnType.INTEGER));
    csvMetaData.getTableDescription().setColumns(columns);

    return csvMetaData;
  }

  public static MetaData getMetaData() {
    MetaData metaData = new MetaData();
    metaData.setName("metadata");
    metaData.setId("585a57136d31212d0ad5fca6");
    DataDescriptor data = new DataDescriptor(metaData);
    data.setUri("csv://test/temp.csv");
    data.setPath("kpmg/lcm/test");
    metaData.set(data.getSectionName(), data.getMap());

    GeneralInfoDescriptor generalInfo = new GeneralInfoDescriptor(metaData);
    generalInfo.setOwner("KPMG");
    generalInfo.setDescription("Sample description");
    metaData.set(generalInfo.getSectionName(), generalInfo.getMap());

    return metaData;
  }

  public static MetaDataWrapper getHiveMetaDataWrapper() {
    TabularMetaData tabularMetaData = new TabularMetaData();
    tabularMetaData.setName("Hive-metadata");
    tabularMetaData.setId("585a57236d31212d0ad5fca6");
    tabularMetaData.getData().setUri("hive://remote-hive-foodmart/product");
    tabularMetaData.getData().setPath("kpmg/lcm/test");
    tabularMetaData.getGeneralInfo().setOwner("KPMG");
    tabularMetaData.getGeneralInfo().setDescription("Sample description");
    List columns = new ArrayList();
    columns.add(new ColumnDescription("name", ColumnType.STRING));
    columns.add(new ColumnDescription("age", ColumnType.INTEGER));
    tabularMetaData.getTableDescription().setColumns(columns);

    return tabularMetaData;
  }

  public static MetaDataWrapper getMongoMetaDataWrapper() {
    TabularMetaData tabularMetaData = new TabularMetaData();
    tabularMetaData.setName("Mongo-metadata");
    tabularMetaData.setId("585a57236d31212d0ad5fca6");
    tabularMetaData.getData().setUri("mongo://mongoStorage/mock");
    tabularMetaData.getData().setPath("kpmg/lcm/test");
    tabularMetaData.getGeneralInfo().setOwner("KPMG");
    tabularMetaData.getGeneralInfo().setDescription("Sample description");
    List columns = new ArrayList();
    columns.add(new ColumnDescription("name", ColumnType.STRING));
    columns.add(new ColumnDescription("age", ColumnType.INTEGER));
    tabularMetaData.getTableDescription().setColumns(columns);

    return tabularMetaData;
  }
}
