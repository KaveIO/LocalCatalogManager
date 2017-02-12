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

import nl.kpmg.lcm.validation.Notification;

/**
 *
 * @author shristov
 */
public class DynamicDataDescriptor extends AbstractMetaDataDescriptor {

  public DynamicDataDescriptor(MetaData metaData) {
    super(metaData);
  }

  public final String getUpdateTimestamp() {

    return get("update-timestamp");
  }

  public final void setUpdateTimestamp(final String timestamp) {

    set("update-timestamp", timestamp);
  }

  public final Long getSize() {

    return get("size");
  }

  public final void setSize(final Long size) {

    set("size", size);
  }

  public final String getState() {

    return get("state");
  }

  public final void setState(final String state) {

    set("state", state);
  }

  public final String getReadable() {

    return get("readable");
  }

  public final void setReadable(final String readable) {

    set("readable", readable);
  }

  @Override
  public String getSectionName() {
    return "dynamic.data";
  }

  @Override
  public void validate(Notification notification) {
    if (getMap() == null) {
      notification.addError("Error: Section \"" + getSectionName()
          + "\" is not found in the metadata!");
      return;
    }
    validateField("readable", notification);
    validateField("state", notification);
    validateField("size", notification);
    validateField("update-timestamp", notification);
  }
}
