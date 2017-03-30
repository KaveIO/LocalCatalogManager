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

  /**
   * 
   * @return the time of the last change of the actual data. the returned value is in unix time
   *         stamp (milliseconds)
   */
  public final Long getDataUpdateTimestamp() {
    Object value =  get("data-update-timestamp");
    return parseLong(value);
  }


  /**
   * 
   * @param timestamp : the time of the last change of the actual data. the value must be is in unix
   *        time stamp (milliseconds)
   */
  public final void setDataUpdateTimestamp(final long timestamp) {

    set("data-update-timestamp", timestamp);
  }

  /**
   * 
   * @return the last time when dynamic data has been collected. the returned value is in unix time
   *         stamp (milliseconds)
   */
  public final Long getUpdateTimestamp() {
    Object value =  get("update-timestamp");
    return parseLong(value);
  }

  /**
   * 
   * @param timestamp: set the last time when dynamic data has been collected. the value must be is
   *        in unix time stamp (milliseconds)
   */
  public final void setUpdateTimestamp(final long timestamp) {

    set("update-timestamp", timestamp);
  }

  /**
   * 
   * @return the duration of the last collection of dynamic data . the returned value is in unix
   *         time stamp (milliseconds)
   */
  public final Long getUpdateDurationTimestamp() {
    Object value =  get("update-duration-timestamp");
    return parseLong(value);
  }

  /**
   * 
   * @param timestamp: the duration of the last collection of dynamic data . the value must be is in
   *        unix time stamp (milliseconds)
   */
  public final void setUpdateDurationInMillis(final long timestamp) {

    set("update-duration-timestamp", timestamp);
  }

  /**
   * 
   * @return the size of the actual data in bytes or null if size is not applicable for the data.
   */
  public final Long getSize() {
    Object value =  get("size");
    return parseLong(value);
  }

  private Long parseLong(Object value) {
    if (value instanceof Long) {
      return (Long) value;
    } else if (value instanceof Integer) {
      return ((Integer) value).longValue();
    } else {
      return null;
    }
  }

  public final void setSize(final long size) {
    set("size", size);
  }

  /**
   * 
   * @return the count of the items in the data or null if items count is not applicable for the
   *         data.
   */
  public final Long getItemsCount() {
    Object value =  get("count");
    return parseLong(value);
  }

  /**
   * 
   * Set the number of the items in the data.
   * 
   * @param count: the items count.
   */
  public final void setItemsCount(final Long count) {

    set("count", count);
  }

  /**
   * 
   * @return the state of the data. If it accessible directly from this instance of LCM the returns
   *         "ATTACHED". This means that it is on the local disk or on a service(HIVE,SQL, Mongo)
   * 
   *         If the data is not accessible(not persisting in the storage where its data.uri points)
   *         returns "DETACHED".
   */
  public final String getState() {

    return get("state");
  }

  /**
   * 
   * @param state: pass "ATTACHED" when the data persist in the storage where its data.uri points.
   *        pass "DETACHED" when the data DOES NOT persist in the storage where its data.uri points.
   */
  public final void setState(final String state) {

    set("state", state);
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
    validateField("state", notification);
    validateField("data-update-timestamp", notification);
  }
}
