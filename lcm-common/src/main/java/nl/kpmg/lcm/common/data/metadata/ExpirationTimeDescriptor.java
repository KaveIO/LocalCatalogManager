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

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author shristov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpirationTimeDescriptor extends AbstractMetaDataDescriptor {
  public static final int MAX_EXPIRATION_YEAR_DURATION = 50;

  public ExpirationTimeDescriptor(MetaData metaData) {
    super(metaData);
  }

  public ExpirationTimeDescriptor(Map map) {
    super(map);
  }

  public final void setExecutionExpirationTime(final String executionExpirationTime) {
    set("execution", executionExpirationTime);
  }

  public final String getExecutionExpirationTime() {
    return get("execution");
  }

  public final void setTransferExpirationTime(final String transferExpirationTime) {
    set("transfer", transferExpirationTime);
  }

  public final String getTransferExpirationTime() {
    return get("transfer");
  }

  @Override
  public String getSectionName() {
    return "expiration-time";
  }

  @Override
  public void validate(Notification notification) {
    if (getMap() != null) {
      validateExpirationTime("execution", notification);
      validateExpirationTime("transfer", notification);
    }
  }

  private void validateExpirationTime(String fieldName, Notification notification) {
    validateField(fieldName, notification);
    if (!notification.hasErrors()) {
      if (!(getMap().get(fieldName) instanceof String)) {
        notification.addError("Error: Invalid " + fieldName + " expiration time of metadata.");
      }
      validateTimestamp(get(fieldName), notification);
    }
  }

  private void validateTimestamp(String expirationTime, Notification notification) {
    Date currentDate = new Date();
    Calendar currentCal = Calendar.getInstance();
    int currentYear = currentCal.get(Calendar.YEAR);
    int currentMonth = currentCal.get(Calendar.MONTH);
    int currentDay = currentCal.get(Calendar.DAY_OF_MONTH);

    long expirationTimeInMiliseconds =
        convertTimestampSecondsToMiliseconds(expirationTime, notification);
    if (notification.hasErrors()) {
      return;
    }

    Date timestampDate = new Date(expirationTimeInMiliseconds);
    Calendar timestampCal = Calendar.getInstance();
    timestampCal.setTime(timestampDate);
    int timestampYear = timestampCal.get(Calendar.YEAR);
    int timestampMonth = timestampCal.get(Calendar.MONTH);
    int timestampDay = timestampCal.get(Calendar.DAY_OF_MONTH);

    if (timestampYear > currentYear + MAX_EXPIRATION_YEAR_DURATION) {
      notification.addError("The expiration time of the metadata is too late in the future: "
          + expirationTime + ".");
    } else if (timestampYear <= currentYear && timestampMonth <= currentMonth
        && timestampDay <= currentDay) {
      notification.addError("The expiration time of the metadata has already passed: "
          + expirationTime + ".");
    }
  }

  private long convertTimestampSecondsToMiliseconds(String timestamp, Notification notification) {
    try {
      return Long.parseLong(timestamp) * 1000;
    } catch (Exception ex) {
      notification.addError("Invalid expiration time of the metadata. Error: " + ex.getMessage());
      return -1;
    }
  }

}
