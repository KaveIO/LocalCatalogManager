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

import nl.kpmg.lcm.server.data.DataFormat;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.validation.Notification;

import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class MongoStorage extends AbstractStorageContainer {

  public MongoStorage(Storage storage) {
    super(storage);
  }

  @Override
  protected void validate(Storage storage, Notification notification) {
    if (!getSupportedStorageTypes().contains(storage.getType())) {
      notification.addError("Storage validation: storage type does not match!");
    }

    if (getHostname() == null || getHostname().isEmpty()) {
      notification.addError("Storage validation: \"hostname\" is missing or is empty!", null);
    }

    if (getPort() == null || getPort().isEmpty() || !StringUtils.isNumeric(getPort())) {
      notification.addError(
          "Storage validation: \"port\" is missing or its value is not a number!", null);
    }

    if (getPassword() == null || getUsername() == null) {
      notification.addError("Storage validation: \"username\" or/and \"password\" is missing!",
          null);
    }

    if (getDatabase() == null || getDatabase().isEmpty()) {
      notification.addError("Storage validation: \"database\" is missing or is empty!", null);
    }
  }

  public String getHostname() {
    return (String) storage.getOptions().get("hostname");
  }

  public String getPort() {
    return (String) storage.getOptions().get("port");
  }

  public String getDatabase() {
    return (String) storage.getOptions().get("database");
  }

  public String getUsername() {
    return (String) storage.getOptions().get("username");
  }

  public String getPassword() {
    return (String) storage.getOptions().get("password");
  }

  /**
   *
   * @return a set with supported storages. i.e if the type of the pure storage object is csv then
   *         it could not be passed to Hive Wrapper(HiveStorage)
   */
  public static Set<String> getSupportedStorageTypes() {
    Set result = new HashSet();
    result.add(DataFormat.MONGO);
    return result;
  }
}
