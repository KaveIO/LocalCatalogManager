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
package nl.kpmg.lcm.server.documentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;

/**
 *
 * @author shristov
 */
public class RestApiDocumentationGenerator {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestApiDocumentationGenerator.class
      .getName());
  private static final String FILE_NAME = "endpoint-doc.html";

  private static Integer counter = 1;

  /**
   * Main method.
   *
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    scan("nl.kpmg.lcm.server.rest");
  }

  private static void scan(String packageName) {
    FileWriter writer = null;
    try {

      Iterable<Class> classes = ClassScanner.getClasses(packageName);

      writer = createFileWriter();

      for (Class<?> clazz : classes) {
        processClass(clazz, writer);
      }

      LOGGER.info("Rest api documentation generated successfully to file: " + FILE_NAME);
    } catch (Exception ex) {
      LOGGER.error("Unable to scan: " + ex.getMessage());
    } finally {
      safelyCloseWriter(writer);
    }
  }

  private static void processClass(Class<?> clazz, FileWriter writer) throws IOException,
      SecurityException {
    String basePath = "";

    if (clazz.getAnnotation(Path.class) != null) {
      basePath = clazz.getAnnotation(Path.class).value();
    }
    for (Method method : clazz.getMethods()) {
      if (method.isAnnotationPresent(RolesAllowed.class)) {
        MethodDocumentator documentator = new MethodDocumentator(method);
        String description = documentator.processMethod(counter, basePath);
        writer.write(description);
        counter++;
      }
    }
  }

  private static void safelyCloseWriter(FileWriter writer) {
    if (writer != null) {
      try {
        writer.close();
      } catch (IOException ex) {
        java.util.logging.Logger.getLogger(RestApiDocumentationGenerator.class.getName()).log(
            Level.SEVERE, null, ex);
      }
    }
  }

  private static FileWriter createFileWriter() {
    try {
      FileWriter writer = new FileWriter(new File(FILE_NAME));
      return writer;
    } catch (IOException ex) {
      LOGGER.error(ex.getMessage());
      return null;
    }
  }
}
