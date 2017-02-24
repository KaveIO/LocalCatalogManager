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

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author shristov
 */
public class MethodDocumentator {
  private Method method;

  private final static String ENDPOINT_DESCRIPTION_TEMPLATE =
      "<br><p><b>{NUMBER}. Path: {PATH}</b></p><p><b>HTTP method</b>: {HTTP_METHOD}</p>"
          + " <p><b>Allowed roles</b>: {ROLES}</p>" + " {CONSUMES}"
          + " <p><b>Parameters</b>: {PARAMETERS}</p>" + " {PRODUCES}"
          + " <p><b>Returns data</b>: {RETURN_DATA}</p>";


  private final static String parameterTemplate = "{NUMBER}. {ANNOTATION} {TYPE}<br>";

  MethodDocumentator(Method method) {
    this.method = method;
  }

  public String processMethod(Integer number, String basePath) throws IOException {
    String description =
        StringUtils.replace(ENDPOINT_DESCRIPTION_TEMPLATE, "{NUMBER}", number.toString());

    description = addPath(method, basePath, description);
    description = addProduces(method, description);
    description = addConsumes(method, description);
    description = addHttpMethod(method, description);
    description = addRoles(method, description);
    description = addReturnedData(method, description);
    description = addParameters(method, description);

    return description;
  }

  private String addParameters(Method method, String description) {
    String parameters = "";
    int counter = 1;
    for (Parameter param : method.getParameters()) {
      String parameterString =
          StringUtils.replace(parameterTemplate, "{NUMBER}", Integer.toString(counter));
      String annotationString = "";
      for (Annotation annotation : param.getAnnotations()) {
        annotationString += annotation.annotationType().getSimpleName();
      }

      if (annotationString.length() > 0) {
        annotationString = annotationString + ", ";
        parameterString = StringUtils.replace(parameterString, "{ANNOTATION}", annotationString);
      } else {
        parameterString = StringUtils.replace(parameterString, "{ANNOTATION}", "Payload, ");
      }

      parameterString =
          StringUtils
              .replace(parameterString, "{TYPE}", "type: " + param.getType().getSimpleName());
      parameters += parameterString;
      counter++;
    }
    if (parameters.length() > 0) {
      parameters = "<br> " + parameters;
    } else {
      parameters = "none.";
    }
    description = StringUtils.replace(description, "{PARAMETERS}", parameters);
    return description;
  }

  private String addReturnedData(Method method, String description) {
    String returnedClass = method.getReturnType().getSimpleName();
    description = StringUtils.replace(description, "{RETURN_DATA}", returnedClass);
    return description;
  }

  private String addRoles(Method method, String description) {
    String[] allowedRoles = method.getAnnotation(RolesAllowed.class).value();
    String roles = mergeStringArray(allowedRoles);
    description = StringUtils.replace(description, "{ROLES}", roles);
    return description;
  }

  private String addHttpMethod(Method method, String description) {
    String type = getMethodType(method);
    description = StringUtils.replace(description, "{HTTP_METHOD}", type);
    return description;
  }

  private String addConsumes(Method method, String description) {
    String[] consumes = null;
    if (method.getAnnotation(Consumes.class) != null) {
      consumes = method.getAnnotation(Consumes.class).value();
    }
    if (consumes != null) {
      String consumesString = mergeStringArray(consumes);
      consumesString = "<p><b>Consumes</b> : " + consumesString + "</p>";
      description = StringUtils.replace(description, "{CONSUMES}", consumesString);
    } else {
      description = StringUtils.replace(description, "{CONSUMES}", "");
    }
    return description;
  }

  private String addProduces(Method method, String description) {
    String[] produces = null;
    if (method.getAnnotation(Produces.class) != null) {
      produces = method.getAnnotation(Produces.class).value();
    }
    if (produces != null) {
      String producesString = mergeStringArray(produces);
      producesString = "<p><b>Produces</b> : " + producesString + "</p>";
      description = StringUtils.replace(description, "{PRODUCES}", producesString);
    } else {
      description = StringUtils.replace(description, "{PRODUCES}", "");
    }
    return description;
  }

  private String addPath(Method method, String basePath, String description) {
    String path = "";
    if (method.getAnnotation(Path.class) != null) {
      path = method.getAnnotation(Path.class).value();
    }
    String fullPath = buildFullPath(basePath, path);
    description = StringUtils.replace(description, "{PATH}", fullPath);
    return description;
  }

  private String mergeStringArray(String[] stringArray) {
    String mergedArray = "";

    if (stringArray == null) {
      return null;
    }

    for (String element : stringArray) {
      mergedArray += element;
      if (element != stringArray[stringArray.length - 1]) {
        mergedArray += ", ";
      }
    }
    return mergedArray;
  }

  private String getMethodType(Method method) {
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

  private String buildFullPath(String basePath, String path) {
    StringBuilder fullPath = new StringBuilder();
    fullPath.append(basePath);
    Character slash = '/';
    if ((basePath.length() == 0 || !slash.equals(basePath.charAt(basePath.length() - 1)))
        && (path.length() == 0 || !slash.equals(path.charAt(0)))) {
      fullPath.append(slash);
    }
    fullPath.append(path);

    return fullPath.toString();
  }
}
