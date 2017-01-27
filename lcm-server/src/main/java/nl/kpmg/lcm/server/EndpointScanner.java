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
package nl.kpmg.lcm.server;

import nl.kpmg.lcm.server.rest.authentication.Roles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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
    scan();
  }

  /**
   * Scans all classes accessible from the context class loader which belong to the given package
   * and subpackages.
   *
   * @param packageName The base package
   * @return The classes
   * @throws ClassNotFoundException
   * @throws IOException
   */
  private static Iterable<Class> getClasses(String packageName) throws ClassNotFoundException,
      IOException, URISyntaxException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String path = packageName.replace('.', '/');
    Enumeration<URL> resources = classLoader.getResources(path);
    List<File> dirs = new ArrayList<File>();
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      URI uri = new URI(resource.toString());
      dirs.add(new File(uri.getPath()));
    }
    List<Class> classes = new ArrayList<Class>();
    for (File directory : dirs) {
      classes.addAll(findClasses(directory, packageName));
    }

    return classes;
  }

  /**
   * Recursive method used to find all classes in a given directory and subdirs.
   *
   * @param directory The base directory
   * @param packageName The package name for classes found inside the base directory
   * @return The classes
   * @throws ClassNotFoundException
   */
  private static List<Class> findClasses(File directory, String packageName)
      throws ClassNotFoundException {
    List<Class> classes = new ArrayList<Class>();
    if (!directory.exists()) {
      return classes;
    }
    File[] files = directory.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        classes.addAll(findClasses(file, packageName + "." + file.getName()));
      } else if (file.getName().endsWith(".class")) {
        classes.add(Class.forName(packageName + '.'
            + file.getName().substring(0, file.getName().length() - 6)));
      }
    }
    return classes;
  }

  private static void scan() {
    FileWriter writer = null;
    try {
      writer = createFileWriter();
      initFile(writer);
      Iterable<Class> classes = getClasses(EndpointScanner.class.getPackage().getName());

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
        java.util.logging.Logger.getLogger(EndpointScanner.class.getName()).log(Level.SEVERE, null, ex);
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
