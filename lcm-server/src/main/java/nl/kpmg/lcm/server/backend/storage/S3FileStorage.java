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

import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.validation.Notification;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class S3FileStorage extends AbstractStorageContainer {

  public S3FileStorage(Storage storage) {
    super(storage);
  }

  public String getAwsAccessKey() {
    return (String) storage.getOptions().get("aws-access-key");
  }

  public String getAwsSecretAccessKey() {
    return (String) storage.getOptions().get("aws-secret-access-key");
  }

  public String getName() {
    return storage.getName();
  }

  @Override
  protected void validate(Storage storage, Notification notification) {
    String storagePath = (String) storage.getOptions().get("storagePath");
    if (storagePath == null || storagePath.isEmpty()) {
      notification.addError("Storage path is missing or is empty!", null);
    }
  }
}
