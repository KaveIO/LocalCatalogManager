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

import nl.kpmg.lcm.validation.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shristov
 */
public class ColumnMapDescriptor {
  private Map map;
  private static final Logger LOGGER = LoggerFactory.getLogger(ColumnMapDescriptor.class.getName());

  public ColumnMapDescriptor(Map map) {
    this.map = map;
  }

  public ColumnMapDescriptor() {
    map = new HashMap();
  }

  public void setType(String value) {
    map.put("type", value);
  }

  public String getType() {
    return (String) map.get("type");
  }

  public void setSize(Integer value) {
    map.put("size", value);
  }

  public Integer getSize() {
    return (Integer) map.get("size");
  }

  public void setPrecision(Integer value) {
    map.put("precision", value);
  }

  public Integer getPrecision() {
    return (Integer) map.get("precision");
  }

  public void validate(Notification notification) {
    if (map == null) {
      notification.addError("Error. Column descriptor can not be initialized with null");
    }

    if (!map.containsKey("type")) {
      LOGGER.debug("Column type is missing.");
      return;
    }

    if (map.get("size") != null) {
      try {
        Integer size = (Integer) map.get("size");
      } catch (ClassCastException cce) {
        LOGGER.warn("\"size\" property is not a integer!");
        notification.addError("\"size\" property is not a integer", cce);
      }
    }

    if (map.get("precision") != null) {
      try {
        Integer precision = (Integer) map.get("precision");
      } catch (ClassCastException cce) {
        LOGGER.warn("\"precision\" property is not a integer!");
        notification.addError("\"precision\" property is not a integer", cce);
      }
    }
  }

  public Map getMap() {
      return map;
  }
}
