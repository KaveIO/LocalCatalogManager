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

import nl.kpmg.lcm.common.Roles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 *
 * @author shristov
 */
public class EndpointScanner {
  private static final Logger LOGGER = LoggerFactory.getLogger(EndpointScanner.class.getName());
  private static final String FILE_NAME = "access-table.html";

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
      writer = createFileWriter();
      initFile(writer);
      Iterable<Class> classes = ClassScanner.getClasses(packageName);

      for (Class<?> clazz : classes) {
        processClass(clazz, writer);
      }
      closeTable(writer);
      LOGGER.info("Access table generated successfully to file: " + FILE_NAME);
    } catch (Exception ex) {
      LOGGER.error("unable to scann: " + ex.getMessage());
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
        processMethod(method, basePath, writer);
      }
    }
  }

  private static void safelyCloseWriter(FileWriter writer) {
    if (writer != null) {
      try {
        writer.close();
      } catch (IOException ex) {
        java.util.logging.Logger.getLogger(EndpointScanner.class.getName()).log(Level.SEVERE, null,
            ex);
      }
    }
  }

  private static void processMethod(Method method, String basePath, FileWriter writer)
      throws IOException {
    String path = "";
    if (method.getAnnotation(Path.class) != null) {
      path = method.getAnnotation(Path.class).value();
    }
    String type = getMethodType(method);
    String[] allowedRoles = method.getAnnotation(RolesAllowed.class).value();
    Map<String, Boolean> rolesMap = new HashMap();
    for (String role : Roles.getRoles()) {
      Boolean match = false;
      for (String allowedRole : allowedRoles) {
        if (role.equals(allowedRole)) {
          match = true;
          break;
        }
      }

      rolesMap.put(role, match);
    }
    StringBuilder row = constructRow(basePath, path, type, rolesMap);
    writer.write(row.toString());
  }

  private static String getMethodType(Method method) {
    String type = "unkown";
    if (method.isAnnotationPresent(GET.class)) {
      type = "GET";
    } else if (method.isAnnotationPresent(POST.class)) {
      type = "POST";
    } else if (method.isAnnotationPresent(PUT.class)) {
      type = "PUT";
    } else if (method.isAnnotationPresent(DELETE.class)) {
      type = "DELETE";
    }
    return type;
  }

  private static StringBuilder constructRow(String basePath, String path, String type,
      Map<String, Boolean> rolesMap) {
    StringBuilder row = new StringBuilder();
    row.append("<tr><td>");
    row.append(basePath);
    Character slash = '/';
    if (!slash.equals(basePath.charAt(basePath.length() - 1)) && slash.equals(path.charAt(0))) {
      row.append(slash);
    }
    row.append(path);
    row.append("</td><td>");
    row.append(type);
    row.append("</td><td>");
    row.append(rolesMap.get(Roles.ADMINISTRATOR) ? "X" : "");
    row.append("</td><td>");
    row.append(rolesMap.get(Roles.API_USER) ? "X" : "");
    row.append("</td><td>");
    row.append(rolesMap.get(Roles.REMOTE_USER) ? "X" : "");
    row.append("</td><td>");
    row.append(rolesMap.get(Roles.ANY_USER) ? "X" : "");
    row.append("</td></tr>");
    return row;
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

  private static void initFile(FileWriter writer) throws IOException {
    if (writer == null) {
      return;
    }
    writer.write("<table>");
    writer
        .write("<tr><th>path</th><th>method</th><th>ADMINISTRATOR</th><th>API_USER</th><th>REMOTE_USER</th><th>ANY_USER</th></tr>");
  }

  private static void closeTable(FileWriter writer) throws IOException {
    if (writer == null) {
      return;
    }
    writer.write("</table>");
  }
}
