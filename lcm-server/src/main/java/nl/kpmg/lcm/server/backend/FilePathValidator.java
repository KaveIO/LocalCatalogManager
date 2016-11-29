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

package nl.kpmg.lcm.server.backend;

import nl.kpmg.lcm.validation.Notification;
import java.io.File;
import java.io.IOException;
import nl.kpmg.lcm.server.backend.exception.DataSourceValidationException;
import org.springframework.stereotype.Service;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */

class FilePathValidator {    
 /**
   * Checks if @subDirectory is sub directory of @baseDirectory 
   * or @subDirectory is file  in sub directory of @baseDirectory.
   * For example: return false for base directory : \temp\myapp\bin\ 
   * and subdirectory ..\\..\file.txt
   * @return true if @subdir File is subdirectory of @baseDir or 
   * @subDirectory is file in sub directory of @baseDirectory
   * @param subDirectory : directory or file that will be checked 
   * @param baseDirectory : base directory against which subDirectory
   * @param notification :  contains messages about checks that were not passed.
   */
  public static boolean validate(File baseDirectory, File subDirectory, Notification notification)
      throws DataSourceValidationException {
    try {
      baseDirectory = baseDirectory.getCanonicalFile();
      subDirectory = subDirectory.getCanonicalFile();

      File parentFile = subDirectory;
      while (parentFile != null) {
        if (baseDirectory.equals(parentFile)) {
          return true;
        }
        parentFile = parentFile.getParentFile();
      }

    } catch (IOException ex) {
      notification.addError("Unable to validate file paths " + baseDirectory.getAbsolutePath() + " and "
          + subDirectory.getPath(), ex);
      return false;
    }
    notification.addError("File path " + subDirectory.getPath() + " is not sub file of "
        + baseDirectory.getAbsolutePath(), null);
    return false;
  }
}
