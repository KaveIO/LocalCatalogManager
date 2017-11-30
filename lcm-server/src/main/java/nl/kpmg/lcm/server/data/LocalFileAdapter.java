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

import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.common.validation.Notification;
import nl.kpmg.lcm.server.backend.FilePathValidator;
import nl.kpmg.lcm.server.backend.storage.LocalFileStorage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author shristov
 */
public class LocalFileAdapter implements FileAdapter {
  private final LocalFileStorage storage;
  private final String fileName;
  private String fullFilePath;

  public LocalFileAdapter(LocalFileStorage storage, String fileName) {
    this.storage = storage;
    this.fileName = fileName;
    fullFilePath = storage.getStoragePath() + fileName;
  }

  @Override
  public void write(InputStream stream, Long size) throws IOException {
    if(size != null && size <= 0) {
        return;
    }

    File destination = new File(fullFilePath);
    FileUtils.copyInputStreamToFile(stream, destination);
  }

  @Override
  public InputStream read() throws IOException {
    return new FileInputStream(fullFilePath);
  }

  @Override
  public boolean exists() throws IOException {
    return (new File(fullFilePath)).exists();
  }

  @Override
  public long length() throws IOException {
    return (new File(fullFilePath)).length();
  }

  @Override
  public long lastModified() throws IOException {
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

  @Override
  public boolean delete() throws Exception{
    return (new File(fullFilePath)).delete();
  }
}
