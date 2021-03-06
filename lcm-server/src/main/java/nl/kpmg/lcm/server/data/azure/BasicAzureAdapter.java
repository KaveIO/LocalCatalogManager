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
package nl.kpmg.lcm.server.data.azure;

import com.microsoft.azure.datalake.store.ADLStoreClient;
import com.microsoft.azure.datalake.store.IfExists;
import com.microsoft.azure.datalake.store.oauth2.AccessTokenProvider;
import com.microsoft.azure.datalake.store.oauth2.ClientCredsTokenProvider;

import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.server.backend.storage.AzureStorage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author shristov
 */
public class BasicAzureAdapter {
  protected ADLStoreClient client;
  protected String storageBasePath;
  protected String filePath;
  private final String PERMISSIONS = "755";

  public BasicAzureAdapter(AzureStorage azureStorage, String filename) {
    AccessTokenProvider provider =
        new ClientCredsTokenProvider(azureStorage.getAuthTokenEndpoint(),
            azureStorage.getClientId(), azureStorage.getClientKey());
    client = ADLStoreClient.createClient(azureStorage.getAccountFQDN(), provider);

    storageBasePath = "/" + azureStorage.getPath();
    filePath = "/" + azureStorage.getPath() + "/" + filename;
  }

  public boolean exists() throws IOException {
    return client.checkExists(filePath);
  }

  public long length() throws IOException {
    return client.getDirectoryEntry(filePath).length;
  }

  public long lastModified() throws IOException {
    return client.getDirectoryEntry(filePath).lastModifiedTime.getTime();
  }

  public boolean delete() throws Exception {
    return client.delete(filePath);
  }

  public void validatePaths() {
    try {
      if (exists()) {
        String filePathx = client.getDirectoryEntry(filePath).fullName;
        if (!filePathx.startsWith(storageBasePath)) {
          throw new LcmValidationException(
              "Metadata path is probably is wrong. Data uri can not contains \"..\\\"!");
        }
      } else if (filePath.contains("../")) {
        throw new LcmValidationException(
            "Metadata path is probbably wrong. Data uri can not contains \"..\\\"!");
      }
    } catch (IOException ex) {
      throw new LcmValidationException("Unable to validate metadata path!");
    }
  }

  public OutputStream getOutputStream() throws IOException {
    OutputStream out = client.createFile(filePath, IfExists.OVERWRITE, PERMISSIONS, true);
    return out;
  }

  public InputStream getInputStream() throws IOException {
    return client.getReadStream(filePath);
  }
}
