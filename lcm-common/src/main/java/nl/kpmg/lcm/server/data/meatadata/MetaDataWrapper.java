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

import java.util.Map;

/**
 *
 * @author shristov
 */
// TODO Refactor this class. Add all needed sub objects
// like general Information,  data  etc.
public class MetaDataWrapper implements MetaDataIdentificator {

  protected final MetaData metaData;

  public MetaDataWrapper(MetaData metaData) {
    this.metaData = metaData;
  }

  public MetaDataWrapper() {
    this.metaData = new MetaData();
  }

  /**
   * @return the id of the object
   */
  public final String getId() {
    return metaData.getId();
  }

  /**
   * @param id the unique id of the object
   */
  public final void setId(final String id) {
    metaData.setId(id);
  }

  @Override
  public String getSourceType() {
    return metaData.getSourceType();
  }

  @Override
  public void setSourceType(String sourceType) {
    metaData.setSourceType(sourceType);
  }

  @Override
  public String getName() {
    return metaData.getName();
  }

  @Override
  public void setName(String name) {
    metaData.setName(name);
  }

  public final String getDataUri() {
    return metaData.get("data.uri");
  }

  public final void setDataUri(final String dataUri) {
    metaData.set("data.uri", dataUri);
  }

  public final Map getDataOptions() {
    return metaData.get("data.options");
  }


  public final void setDataOptions(final Map dataOptions) {
    metaData.set("data.options", dataOptions);
  }

  public final void setDataState(final String value) {
    metaData.set("dynamic.data.state", value);
  }

  public final String getDataState() {
    return metaData.get("dynamic.data.state");
  }

  public final void setDataReadable(final String value) {
    metaData.set("dynamic.data.readable", value);
  }

  public final void setDataSize(final Long value) {
    metaData.set("dynamic.data.size", value);
  }

  public final void setDataUpdateTimestamp(final String value) {
    metaData.set("dynamic.data.update-timestamp", value);
  }

  public final String getOwner() {
    return metaData.get("general.owner");
  }

  public final String getDescription() {
    return metaData.get("general.description");
  }


  /**
   * @return the metaData
   */
  public MetaData getMetaData() {
    return metaData;
  }


  public boolean isEmpty() {
    return metaData == null;
  }
}
