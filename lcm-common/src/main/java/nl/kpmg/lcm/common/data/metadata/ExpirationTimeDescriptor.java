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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 * @author shristov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpirationTimeDescriptor extends AbstractMetaDataDescriptor {
  public static final int MAX_EXPIRATION_YEAR_DURATION = 50;
  public static final long MAX_VALID_UNIX_TIMESTAMP = 2147483647;
  public static final long MIN_VALID_UNIX_TIMESTAMP = 0;

  private static final Logger LOGGER = LoggerFactory.getLogger(ExpirationTimeDescriptor.class);

  public ExpirationTimeDescriptor(MetaData metaData) {
    super(metaData);
  }

  public ExpirationTimeDescriptor(Map map) {
    super(map);
  }

  public final boolean setExecutionExpirationTime(final String executionExpirationTime) {
    if (executionExpirationTime == null) {
      LOGGER.warn("The execution expiration time could not be null.");
      return false;
    }
    set("execution", executionExpirationTime);
    return true;
  }

  public final String getExecutionExpirationTime() {
    return get("execution");
  }

  public final boolean setTransferExpirationTime(final String transferExpirationTime) {
    if (transferExpirationTime == null) {
      LOGGER.warn("The transfer expiration time could not be null.");
      return false;
    }
    set("transfer", transferExpirationTime);
    return true;
  }

  public final String getTransferExpirationTime() {
    return get("transfer");
  }

  public final void removeTransferExpirationTime() {
    remove("transfer");
  }

  @Override
  public String getSectionName() {
    return "expiration-time";
  }

  @Override
  public void validate(Notification notification) {
    if (getMap() != null) {
      if (getMap().get("execution") != null) {
        validateExpirationTime("execution", notification);
      }
      if (getMap().get("transfer") != null) {
        validateExpirationTime("transfer", notification);
      }
    }
  }

  private void validateExpirationTime(String fieldName, Notification notification) {
    if (!(getMap().get(fieldName) instanceof String)) {
      notification.addError("Error: Invalid " + fieldName + " expiration time of metadata.");
    }
    String expirationTimeString = (String) getMap().get(fieldName);
    long exTime = 0;
    try {
      exTime = Long.parseLong(expirationTimeString);
    } catch (Exception ex) {
      notification.addError("Invalid expiration time of the metadata. Error: " + ex.getMessage());
    }
    if (exTime < MIN_VALID_UNIX_TIMESTAMP || exTime > MAX_VALID_UNIX_TIMESTAMP) {
      notification.addError("The expiration times can not be smaller than "
          + MIN_VALID_UNIX_TIMESTAMP + " and bigger than " + MAX_VALID_UNIX_TIMESTAMP + ".");
    }
  }
}
