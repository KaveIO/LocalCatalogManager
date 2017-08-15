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

import org.apache.metamodel.schema.ColumnType;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shristov
 */
public class TableDescriptionDescriptorTest {

  @Test
  public void testReadWrite() {
    MetaData metaData = new MetaData();

    List<ColumnDescription> columnsList = new ArrayList();
    columnsList.add(new ColumnDescription("name", ColumnType.STRING));
    columnsList.add(new ColumnDescription("age", ColumnType.INTEGER));
    
    String testKey = "1283245748395bvbx";
    TableDescriptionDescriptor tableDescription = new TableDescriptionDescriptor(metaData,testKey);
    tableDescription.setColumns(columnsList);
    metaData.set(tableDescription.getSectionName(), tableDescription.getMap());
    Map<String, ColumnDescription> columnsMap = tableDescription.getColumns();
    Assert.assertEquals(columnsMap.size(), columnsList.size());
    for(ColumnDescription description : columnsList){
        Assert.assertNotNull(columnsMap.get(description.getName()));
        Assert.assertEquals(description.getType(), columnsMap.get(description.getName()).getType());
    }

    Assert.assertEquals(1, tableDescription.getMap().size());

  }
}
