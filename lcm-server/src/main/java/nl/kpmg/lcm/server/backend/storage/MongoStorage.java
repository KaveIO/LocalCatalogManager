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
import nl.kpmg.lcm.server.security.EncryptionException;
import nl.kpmg.lcm.common.validation.Notification;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class MongoStorage extends AbstractStorageContainer {
  private static final Logger LOGGER = LoggerFactory.getLogger(MongoStorage.class.getName());

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

    if (getDatabase() == null || getDatabase().isEmpty()) {
      notification.addError("Storage validation: \"database\" is missing or is empty!", null);
    }

    validateCredentials(notification);
  }

  public void validateCredentials(Notification notification) {
    if (storage.getCredentials() == null) {
      notification.addError("Credentials does not exists!");
      return;
    }

    if (getUsername() == null) {
      notification.addError("Storage validation: account is missing or is empty!", null);
    }

    if (getPassword() == null) {
      notification.addError("Storage validation: \"password\" is missing!", null);
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
    if (storage.getCredentials() == null) {
      return null;
    }

    return (String) storage.getCredentials().get("username");
  }


  public void setUsername(String username) {
    storage.getCredentials().put("username", username);
  }

  /**
   *
   * @return unencrypted password
   * @throws EncryptionException when the password is not already encrypted. In such cases you need
   *         first to use encryptPassword method. Also EncryptionException may thrown if the
   *         encryption library does not succeed to decrypt the password in any reason.
   */
  public String getPassword() {
    return (String) storage.getCredentials().get("password");
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

  /**
   * @return a set with credentials fields that needs to be encrypted for this storage type
   */
  public static Set<String> getEncryptedCredentialsFields() {
    Set result = new HashSet();
    result.add("password");
    return result;
  }
}