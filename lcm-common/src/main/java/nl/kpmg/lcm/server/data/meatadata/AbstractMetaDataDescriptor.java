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
package nl.kpmg.lcm.server.data.meatadata;

import nl.kpmg.lcm.validation.Notification;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shristov
 */
public abstract class AbstractMetaDataDescriptor {
  private final MetaData metaData;

  AbstractMetaDataDescriptor(MetaData metaData) {
    this.metaData = metaData;
  }

  public void createMap() {
    metaData.set(getSectionName(), new HashMap());
  }

  public Map getMap() {
    return metaData.get(getSectionName());
  }

  protected final <T> T get(final String fieldName) {
    if (getMap() == null) {
      return null;
    }

    return (T) getMap().get(fieldName);
  }

  protected final void set(final String fieldName, final Object value) {
    if (getMap() == null) {
      createMap();
    }

    getMap().put(fieldName, value);
  }

  protected final void validateField(String fieldName, Notification notification) {
    if (getMap().get(fieldName) == null) {
      notification.addError("Error: Section \"" + fieldName + "\" is not found");
      return;
    }
  }

  public abstract String getSectionName();

  public abstract void validate(Notification notification);
}
