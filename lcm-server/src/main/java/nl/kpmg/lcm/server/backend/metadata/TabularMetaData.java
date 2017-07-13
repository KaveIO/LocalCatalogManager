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
import nl.kpmg.lcm.server.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.data.metadata.Wrapper;
import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.validation.Notification;

import java.util.Map;

/**
 *
 * @author shristov
 */
@Wrapper
public class TabularMetaData extends MetaDataWrapper {

  public TabularMetaData(MetaData metaData) {
    super(metaData);
    Map<String, DataItemsDescriptor> allDataDescriptors = getDynamicData().getAllDynamicDataDescriptors();
    if(getDynamicData().getAllDynamicDataDescriptors() == null) {
        return;
    }
    Notification notification = new Notification();
    for(String key: allDataDescriptors.keySet()){
        TabularDynamicDataDescriptor newDescriptor = new TabularDynamicDataDescriptor(metaData, key);
        newDescriptor.getTableConfigurationDescriptor().validate(notification);
        newDescriptor.getTableDescriptionDescriptor().validate(notification);
    }
    if (notification.hasErrors()) {
      throw new LcmValidationException(notification);
    }
  }

  public TabularMetaData() {
    super();
    
  }

  public final void setTableDescription(final TableDescriptionDescriptor value) {
    metaData.set(value.getSectionName(), value.getMap());
  }

  public final TableDescriptionDescriptor getTableDescription(String key) {
      
    return new TableDescriptionDescriptor(metaData, key);
  }

  public final void setTableConfiguration(final TableConfigurationDescriptor value) {
    metaData.set(value.getSectionName(), value.getMap());
  }

  public final TableConfigurationDescriptor getTableConfiguration(String key) {
      return new TableConfigurationDescriptor(metaData, key);
  }

}
