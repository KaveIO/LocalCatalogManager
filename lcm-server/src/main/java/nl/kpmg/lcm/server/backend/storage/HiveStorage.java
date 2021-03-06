/*
 * Copyright 2016 KPMG N.V. (unless otherwise stated).
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

package nl.kpmg.lcm.server.backend.storage;

import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.validation.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class HiveStorage extends AbstractStorageContainer {
  private static final Logger LOGGER = LoggerFactory.getLogger(HiveStorage.class.getName());

  public HiveStorage(Storage storage) {
    super(storage);
  }

  @Override
  protected void validate(Storage storage, Notification notification) {
    if (!getSupportedStorageTypes().contains(storage.getType())) {
      notification.addError("Storage validation: storage type does not match!");
    }

    String url = (String) storage.getOptions().get("url");
    if (url == null || url.isEmpty()) {
      notification.addError("Storage validation: url is missing or is empty!", null);
    }

    if (getDriver() == null || getDriver().isEmpty()) {
      notification.addError("Storage validation: driver is missing or is empty!", null);
    }

    if (getDatabase() == null || getDatabase().isEmpty()) {
      notification.addError("Storage validation: database is missing or is empty!", null);
    }
  }

  public String getUrl() {
    return (String) storage.getOptions().get("url");
  }

  public String getDatabase() {
    return (String) storage.getOptions().get("database");
  }

  public String getUsername() {
    return (String) storage.getCredentials().get("username");
  }

  /**
   *
   * @return unencrypted password
   */
  public String getPassword() {
    return (String) storage.getCredentials().get("password");
  }

  public String getDriver() {
    return (String) storage.getOptions().get("driver");
  }

  /**
   *
   * @return a set with supported storages. i.e if the type of the pure storage object is csv then
   *         it could not be passed to Hive Wrapper(HiveStorage)
   */
  public static Set<String> getSupportedStorageTypes() {
    Set result = new HashSet();
    result.add(DataFormat.HIVE);
    return result;
  }

  /**
   * @return a set with credentials fields that needs to be encrypted for this storage type
   */
  public static Set<String> getEncryptedCredentialsFields() {
    Set result = new HashSet();
    result.add("password");
    return result;
  }
}
