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

import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.common.validation.Notification;
import nl.kpmg.lcm.server.backend.BackendCsvImpl;
import nl.kpmg.lcm.server.backend.FilePathValidator;
import nl.kpmg.lcm.server.backend.storage.LocalFileStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author shristovs
 */
public class LocalCsvAdapter implements CsvAdapter {
  private static final Logger LOGGER = LoggerFactory.getLogger(BackendCsvImpl.class.getName());
  private final LocalFileStorage storage;
  private final String fileName;

  public LocalCsvAdapter(LocalFileStorage storage, String filePath) {
    this.storage = storage;
    this.fileName = filePath;
  }

  private File constructDatasourceFile() {
    File dataSourceFile;
    File baseDir = new File(storage.getStoragePath());

    dataSourceFile = new File(storage.getStoragePath() + fileName);

    Notification notification = new Notification();
    FilePathValidator.validate(baseDir, dataSourceFile, notification);

    if (notification.hasErrors()) {
      throw new LcmValidationException(notification);
    }

    return dataSourceFile;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    File dataSourceFile = constructDatasourceFile();
    OutputStream out = new FileOutputStream(dataSourceFile);
    return out;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    File dataSourceFile = constructDatasourceFile();
    if (!dataSourceFile.exists()) {
      throw new LcmException("Unable to find data source file! FilePath: "
          + dataSourceFile.getPath());
    }
    return new FileInputStream(dataSourceFile);
  }

  @Override
  public boolean exists() throws IOException {
    String fullFilePath = storage.getStoragePath() + fileName;
    return (new File(fullFilePath)).exists();
  }

  @Override
  public long length() throws IOException {
    String fullFilePath = storage.getStoragePath() + fileName;
    return (new File(fullFilePath)).length();
  }

  @Override
  public long lastModified() throws IOException {
    String fullFilePath = storage.getStoragePath() + fileName;
    return (new File(fullFilePath)).lastModified();
  }

  @Override
  public void validatePaths() {
    File baseDir = new File(storage.getStoragePath());
    File dataSourceFile = new File(storage.getStoragePath() + fileName);
    Notification notification = new Notification();
    FilePathValidator.validate(baseDir, dataSourceFile, notification);

    if (notification.hasErrors()) {
      throw new LcmValidationException(notification);
    }
  }
}
