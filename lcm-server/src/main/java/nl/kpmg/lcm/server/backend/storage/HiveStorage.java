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

import nl.kpmg.lcm.server.backend.exception.BackendException;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.validation.Notification;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class HiveStorage extends AbstractStorageContainer {

  public HiveStorage(Storage storage) throws BackendException {
    super(storage);
  }

  @Override
  protected void validate(Storage storage, Notification notification) {
    String url = (String) storage.getOptions().get("url");
    if (url == null || url.isEmpty()) {
      notification.addError("Storage validation: url is missing or is empty!", null);
    }

    String password = (String) storage.getOptions().get("password");
    String username = (String) storage.getOptions().get("username");
    if (password == null || password.isEmpty() || username == null || username.isEmpty()) {
      notification.addError("Storage validation: account is missing or is empty!", null);
    }

    String driver = (String) storage.getOptions().get("driver");
    if (driver == null || driver.isEmpty()) {
      notification.addError("Storage validation: driver is missing or is empty!", null);
    }

    String database = (String) storage.getOptions().get("database");
    if (database == null || database.isEmpty()) {
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
    return (String) storage.getOptions().get("username");
  }

  public String getPassword() {
    return (String) storage.getOptions().get("password");
  }

  public String getDriver() {
    return (String) storage.getOptions().get("driver");
  }
}
