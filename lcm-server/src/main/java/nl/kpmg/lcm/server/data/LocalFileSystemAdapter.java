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
package nl.kpmg.lcm.server.data;

import nl.kpmg.lcm.common.data.TestResult;
import nl.kpmg.lcm.server.backend.storage.LocalFileStorage;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author shristov
 */
public class LocalFileSystemAdapter implements FileSystemAdapter{
  private final LocalFileStorage storage;

  public LocalFileSystemAdapter(LocalFileStorage storage) {
    this.storage = storage;
  }

  /**
   *
   * @param subPath
   * @return the list of files in the give sub directory  and null is @subPath is not found.
   * @throws IOException
   */
   @Override
   public List listFileNames(String subPath) throws IOException {
    String storagePath = storage.getStoragePath();
    File dataSourceDir = new File(storagePath + "/" + subPath);

    if (!dataSourceDir.exists() || !dataSourceDir.isDirectory()) {
      return null;
    }

    File[] files = dataSourceDir.listFiles();
    List<String> fileNameList = new LinkedList();
    for (File file : files) {
      if (file.isFile()) {
        fileNameList.add(file.getName());
      }
    }

    return fileNameList;
    }

  @Override
  public TestResult testConnection() throws IOException {
    File baseDir = new File(storage.getStoragePath());
    if (baseDir.exists() && baseDir.isDirectory()) {
      return new TestResult("OK", TestResult.TestCode.ACCESIBLE);
    } else {
      return new TestResult("Storage directory does not exists", TestResult.TestCode.INACCESSIBLE);
    }
  }

}
