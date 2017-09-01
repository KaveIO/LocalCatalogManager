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

import com.microsoft.azure.datalake.store.IfExists;

import nl.kpmg.lcm.server.backend.storage.AzureStorage;
import nl.kpmg.lcm.server.data.CsvAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author shristov
 */
public class AzureCsvAdapter extends BasicAzureAdapter implements CsvAdapter {
  public AzureCsvAdapter(AzureStorage azureStorage, String filename) {
    super(azureStorage, filename);
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    OutputStream out = client.createFile(filePath, IfExists.OVERWRITE);
    return out;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return client.getReadStream(filePath);
  }
}
