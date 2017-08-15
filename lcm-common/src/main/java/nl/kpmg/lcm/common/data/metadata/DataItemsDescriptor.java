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

import com.fasterxml.jackson.annotation.JsonInclude;

import nl.kpmg.lcm.common.validation.Notification;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shristov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataItemsDescriptor extends AbstractMetaDataDescriptor {

  private String key;
  public DataItemsDescriptor(MetaData metaData, String key) {
    super(metaData);
    this.key = key;
  }

  public DataItemsDescriptor(Map map) {
    super(map);
  }

  /**
   *
   * @return the time of the last change of the actual data. the returned value is in unix time
   *         stamp (milliseconds)
   */
  public final String getURI() {
    return get("uri");
  }


  public final void setURI(final String uri) {

    set("uri", uri);
  }

  public final DataDetailsDescriptor getDetailsDescriptor() {
    if(get("details") ==  null) {
        return new DataDetailsDescriptor(metaData, key);
    }

    return new DataDetailsDescriptor(get("details"));
  }


  public final void setDetailsDescriptor(final DataDetailsDescriptor details) {

    set("details", details.getMap());
  }

  public final void clearDetailsDescriptor() {

    set("details", new HashMap());
  }

  @Override
  public String getSectionName() {
    return "dynamic.data.items." + key;
  }

  @Override
  public void validate(Notification notification) {
    if (getMap() == null) {
      notification.addError("Error: Section \"" + getSectionName()
          + "\" is not found in the metadata!");
      return;
    }
  }
}