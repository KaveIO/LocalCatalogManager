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

import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.validation.Notification;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author shristov
 */
public class AzureStorage extends AbstractStorageContainer {

  public AzureStorage(Storage storage) {
    super(storage);
  }

  public String getAccountFQDN() {
    return (String) storage.getCredentials().get("account-FQDN");
  }

  public void setAccountFQDN(String accountFQDN) {
    storage.getCredentials().put("account-FQDN", accountFQDN);
  }

  public String getClientId() {
    return (String) storage.getCredentials().get("client-id");
  }

  public void setClientId(String clientId) {
    storage.getCredentials().put("client-id", clientId);
  }

  public String getAuthTokenEndpoint() {
    return (String) storage.getCredentials().get("auth-token-endpoint");
  }

  public void setAuthTokenEndpoint(String authTokenEndpoint) {
    storage.getCredentials().put("auth-token-endpoint", authTokenEndpoint);
  }

  public String getClientKey() {
    return (String) storage.getCredentials().get("client-key");
  }

  public void setClientKey(String clientKey) {
    storage.getCredentials().put("client-key", clientKey);
  }

  public String getPath() {
    return (String) storage.getOptions().get("path");
  }

  public void setPath(String path) {
      storage.getOptions().put("path",  path);
  }

  @Override
  protected void validate(Storage storage, Notification notification) {
    if (!getSupportedStorageTypes().contains(storage.getType())) {
      notification.addError("Storage validation: storage type does not match!");
    }

    String storagePath = (String) storage.getOptions().get("path");
    if (storagePath == null || storagePath.isEmpty()) {
      notification.addError("Storage path is missing or is empty!", null);
    }

    validateCredentials(notification);
  }

  public void validateCredentials(Notification notification) {
    if (storage.getCredentials() == null) {
      notification.addError("Credentials does not exists!");
      return;
    }

    if (getAccountFQDN() == null) {
      notification.addError("Storage validation: \"account-FQDN\" is missing!", null);
    }

    if (getClientId() == null) {
      notification.addError("Storage validation: \"client-id\" is missing!", null);
    }

    if (getClientKey() == null) {
      notification.addError("Storage validation: \"client-key\" is missing!", null);
    }

    if (getAuthTokenEndpoint() == null) {
      notification.addError("Storage validation: \"auth-token-endpoint\" is missing!", null);
    }
  }

  /**
   *
   * @return a set with supported storages. i.e if the type of the pure storage object is csv then
   *         it could not be passed to HDFS wrapper(HdfsStorage)
   */
  public static Set<String> getSupportedStorageTypes() {
    Set result = new HashSet();
    result.add(DataFormat.AZUREFILE);
    result.add(DataFormat.AZURECSV);
    return result;
  }

  /**
   * @return a set with credentials fields that needs to be encrypted for this storage type
   */
  public static Set<String> getEncryptedCredentialsFields() {
    Set result = new HashSet();
    result.add("client-key");
    result.add("auth-token-endpoint");
    result.add("client-id");
    result.add("account-FQDN");

    return result;
  }

}
