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

import nl.kpmg.lcm.server.data.metadata.DataItemsDescriptor;
import nl.kpmg.lcm.server.data.metadata.MetaData;

/**
 *
 * @author shristov
 */
public class TabularDynamicDataDescriptor extends DataItemsDescriptor {
    TableDescriptionDescriptor tableDescriptor;
    TableConfigurationDescriptor tableConfiguration;
    public TabularDynamicDataDescriptor(MetaData metaData, String key) {
        super(metaData, key);
        tableDescriptor = new TableDescriptionDescriptor(metaData, key);
        tableConfiguration = new TableConfigurationDescriptor(metaData, key);
        
    }
    
    public final TableDescriptionDescriptor getTableDescriptionDescriptor() {
    return tableDescriptor;
  }


  public final void setTableDescriptionDescriptor(final TableDescriptionDescriptor details) {
    set("table-description", details.getMap());
  }
  
  public final TableConfigurationDescriptor getTableConfigurationDescriptor() {
    return tableConfiguration;
  }


  public final void setTableConfigurationDescriptor(final TableConfigurationDescriptor details) {
    set("table-configuration", details.getMap());
  }
}
