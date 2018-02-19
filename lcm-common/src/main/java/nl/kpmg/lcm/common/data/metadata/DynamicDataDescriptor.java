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
package nl.kpmg.lcm.common.data.metadata;

import nl.kpmg.lcm.common.validation.Notification;

import java.util.HashMap;
import java.util.Map;

/**
 * Divide this file in two in case you need separate description "items" in separate descriptor.
 *
 * @author shristov
 */
public class DynamicDataDescriptor extends AbstractMetaDataDescriptor {

  private MetaData metadata;
  Map items;

  public DynamicDataDescriptor(MetaData metadata) {
    super(metadata);
    this.metadata = metadata;
  }

  public DynamicDataDescriptor(Map map) {
    super(map);
  }

  private Map getItems() {
    if (getMap() == null) {
      return null;
    }
    return (Map) getMap().get("items");
  }

  public DataItemsDescriptor getDynamicDataDescriptor(String key) {
    if (getItems() == null) {
      return null;
    }

    if (getItems().get(key) == null) {
      getItems().put(key, (new DataItemsDescriptor(metadata, key)).getMap());
    }

    return new DataItemsDescriptor(metadata, key);
  }

  public Map<String, DataItemsDescriptor> getAllDynamicDataDescriptors() {
    HashMap<String, DataItemsDescriptor> result = new HashMap();
    if (getItems() == null) {
      return result;
    }
    for (Object key : getItems().keySet()) {
      result.put((String) key, new DataItemsDescriptor(metadata, (String) key));
    }

    return result;
  }

  public void addDynamicDataDescriptors(String key, Map descriptor) {
    if (getItems() == null) {
      getMap().put("items", new HashMap());
    }

    getItems().put(key, descriptor);
  }

  public void removeDynamicDataItem(String key) {
    if (getItems() == null) {
      return;
    }

    getItems().remove(key);
  }

  public String getSectionName() {
    return "dynamic.data";
  }

  @Override
  public void validate(Notification notification) {
    // Intentially blank. This section is not mandatory and could be missed.
  }

}
