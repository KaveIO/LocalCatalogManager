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
import com.microsoft.azure.datalake.store.DirectoryEntry;
import com.microsoft.azure.datalake.store.DirectoryEntryType;
import com.microsoft.azure.datalake.store.oauth2.AccessTokenProvider;
import com.microsoft.azure.datalake.store.oauth2.ClientCredsTokenProvider;

import nl.kpmg.lcm.common.data.TestResult;
import nl.kpmg.lcm.server.backend.storage.AzureStorage;
import nl.kpmg.lcm.server.data.FileSystemAdapter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author shristov
 */
public class AzureFileSystemAdapter implements FileSystemAdapter {
  private ADLStoreClient client;
  private final AzureStorage storage;

  public AzureFileSystemAdapter(AzureStorage storage) {
    AccessTokenProvider provider =
        new ClientCredsTokenProvider(storage.getAuthTokenEndpoint(), storage.getClientId(),
            storage.getClientKey());
    client = ADLStoreClient.createClient(storage.getAccountFQDN(), provider);

    this.storage = storage;
  }

  @Override
  public List listFileNames(String subPath) throws IOException {
    String storagePath = storage.getPath();
    String dataSourceDir = storagePath + "/" + subPath;

    if (!client.checkExists(dataSourceDir)
        || client.getDirectoryEntry(dataSourceDir).type != DirectoryEntryType.DIRECTORY) {
      return null;
    }

    List<DirectoryEntry> content = client.enumerateDirectory(dataSourceDir);
    List<String> fileNameList = new LinkedList();
    for (DirectoryEntry object : content) {
      if (object.type == DirectoryEntryType.FILE) {
        fileNameList.add(object.fullName);
      }
    }

    return fileNameList;
  }

  @Override
  public TestResult testConnection() throws IOException {
    String storagePath = storage.getPath();
    if (client.checkExists(storagePath)
        && client.getDirectoryEntry(storagePath).type == DirectoryEntryType.DIRECTORY) {
      return new TestResult("OK", TestResult.TestCode.ACCESIBLE);
    } else {
      return new TestResult("Storage directory does not exists", TestResult.TestCode.INACCESSIBLE);
    }
  }

}
