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
package nl.kpmg.lcm.server.backend.storage;

import nl.kpmg.lcm.server.data.DataFormat;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.validation.Notification;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author shristov
 */
public class HdfsFileStorage extends AbstractStorageContainer {

  public HdfsFileStorage(Storage storage) {
    super(storage);
  }

  public String getUrl() {
    return (String) storage.getOptions().get("url");
  }

  public String getPath() {
    return (String) storage.getOptions().get("path");
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


  @Override
  protected void validate(Storage storage, Notification notification) {
    if (!getSupportedStorageTypes().contains(storage.getType())) {
      notification.addError("Storage validation: storage type does not match!");
    }
    if (getPath() == null || getPath().isEmpty()) {
      notification.addError("Storage validation: path is missing or is empty!", null);
    }
    if (getUrl() == null || getUrl().isEmpty()) {
      notification.addError("Storage validation: url is missing or is empty!", null);
    }

    try {
      new URI(getUrl());
    } catch (URISyntaxException ex) {
      notification.addError("Invalid URI. Error: " + ex.getMessage());
    }


  }

  /**
   *
   * @return a set with supported storages. i.e if the type of the pure storage object is csv then
   *         it could not be passed to HDFS wrapper(HdfsStorage)
   */
  public static Set<String> getSupportedStorageTypes() {
    Set result = new HashSet();
    result.add(DataFormat.HDFSFILE);
    return result;
  }
}
