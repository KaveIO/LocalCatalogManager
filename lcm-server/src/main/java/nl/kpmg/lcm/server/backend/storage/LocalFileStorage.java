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

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class LocalFileStorage extends AbstractStorageContainer {

  public LocalFileStorage(Storage storage) {
    super(storage);
  }

  public String getStoragePath() {
    return (String) storage.getOptions().get("storagePath");
  }

  public String getFileType() {
    return storage.getType();
  }

  @Override
  protected void validate(Storage storage, Notification notification) {
    if (!getSupportedStorageTypes().contains(storage.getType())) {
      notification.addError("Storage validation: storage type does not match!");
    }
    String storagePath = (String) storage.getOptions().get("storagePath");
    if (storagePath == null || storagePath.isEmpty()) {
      notification.addError("Storage path is missing or is empty!", null);
    }
  }

  /**
   *
   * @return a set with supported storages. i.e if the type of the pure storage object is csv then
   *         it could not be passed to Hive Wrapper(HiveStorage)
   */
  public static Set<String> getSupportedStorageTypes() {
    Set result = new HashSet();
    result.add(DataFormat.CSV);
    result.add(DataFormat.FILE);
    result.add(DataFormat.JSON);
    return result;
  }
}
