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
 * This class is designed for superclass. It should be inherited by implementation
 * 
 * @author Stoyan Hristov<shristov@intracol.com>
 */
abstract class AbstractStorageContainer {

  protected Storage storage;

  protected AbstractStorageContainer(Storage storage) throws BackendException {
    Notification notification = new Notification();
    basicValidation(storage, notification);
    if(!notification.hasErrors()) {
        validate(storage, notification);
    }

    if (notification.hasErrors()) {
      throw new BackendException(notification.errorMessage());
    }
    this.storage = storage;
  }

  private void basicValidation(Storage storage, Notification notification) {
    if (storage == null) {
      notification.addError("Storage can not be null!", null);
      return;
    }

    if (storage.getOptions() == null) {
      notification.addError("Storage object must have options!", null);
    }
  }

  protected abstract void validate(Storage storage, Notification notification);
}
