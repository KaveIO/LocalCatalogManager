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

import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.exception.LcmException;
import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.validation.Notification;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
@Service
public class BackendFactory {

  private final Logger logger = Logger.getLogger(BackendFactory.class.getName());
  private Map<String, Class<?>> backendClassMap = null;
  private static final int MAX_KEY_LENGTH = 1024;
  private final String backendSourcePackage = "nl.kpmg.lcm.server.backend";

  public Backend createBackend(String sourceType, Storage storage, MetaData metadata) {

    Notification notification = validate(sourceType, storage);

    if (notification.hasErrors()) {
        throw new LcmValidationException(notification);
    }

    if (backendClassMap == null) {
      scan();
    }

    if (backendClassMap == null || backendClassMap.isEmpty()
        || backendClassMap.get(sourceType) == null) {
      throw new LcmException(
          "Backend is not implemented for soruce with type: " + sourceType);
    }

    Class backendClass = backendClassMap.get(sourceType);
    Backend backendImpl = getBackendImplementation(backendClass, storage, metadata);
    return backendImpl;
  }

    private Notification validate(String sourceType, Storage storage) {
        Notification notification = new Notification();
        if (sourceType == null) {
            notification.addError("Data source type could not be null", null);
        } else if (sourceType.isEmpty()) {
            notification.addError("Data source type could not be empty!", null);
        } else if (sourceType.length() > MAX_KEY_LENGTH) {
            notification.addError("Data source type could not be longer then: "
                    + MAX_KEY_LENGTH, null);
        }

        if (storage == null) {
          notification.addError("Storage parameter could not be null", null);
        }

        return notification;
    }

  private void scan() {
    ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(true);

    // TODO it seems that currently the filter is working on half - return only classes
    // that has annotation but doesn't case about annotations type.
    // The code bellow works correct but it will make extra cycles.
    scanner
        .addIncludeFilter(new AnnotationTypeFilter(nl.kpmg.lcm.server.backend.BackendSource.class));
    backendClassMap = new ConcurrentHashMap();
    for (BeanDefinition bd : scanner.findCandidateComponents(backendSourcePackage)) {
      logger.log(Level.FINE, "Found class: {0}", bd.getBeanClassName());
      Class backendClass = getClassForName(bd.getBeanClassName());
      if (backendClass == null) {
        logger.log(Level.WARNING, "Found class with scanner: {0} but was not able to laod it",
            new Object[] {bd.getBeanClassName()});
        continue;
      }

      String sourceType = getSupportedSourceType(backendClass);

      if (sourceType != null) {
        logger.log(Level.FINE,
            "Put to backendClassMap entity with  key: {0} and object of type: {1}",
            new Object[] {sourceType, bd.getBeanClassName()});
        if (backendClassMap.get(sourceType) != null) {
          logger.log(Level.SEVERE,
              "There are more then one Class that implements \"{0}\" source Type!",
              new Object[] {sourceType});
        }
        backendClassMap.put(sourceType, backendClass);
      }
    }
  }

  private Class<?> getClassForName(String className) {

    Class<?> clazz;
    try {
      clazz = Class.forName(className);

      return clazz;
    } catch (ClassNotFoundException ex) {
      logger.log(Level.WARNING, "Unable to find class: " + className, ex);
    }

    return null;
  }

  private Backend getBackendImplementation(Class<?> clazz, Storage storage, MetaData metadata) {
    try {
      Constructor<?> constructor = clazz.getConstructor(Storage.class, MetaData.class);
      Backend backendImpl = (Backend) constructor.newInstance(new Object[] {storage, metadata});

      return backendImpl;
    } catch (NoSuchMethodException ex) {
      logger.log(Level.WARNING, "Unable to find a constructor with Storage class as paraqmeter ",
          ex);
    } catch (SecurityException ex) {
      logger.log(Level.WARNING, null, ex);
    } catch (InstantiationException ex) {
      logger.log(Level.WARNING, "Unable to instantiate " + clazz.getName(), ex);
    } catch (IllegalAccessException ex) {
      logger.log(Level.WARNING, null, ex);
    } catch (IllegalArgumentException ex) {
      logger.log(Level.WARNING, "Unable to call constructor with param: " + storage.getId(), ex);
    } catch (InvocationTargetException ex) {
      logger.log(Level.WARNING, null, ex);
      if (ex.getCause() instanceof LcmException) {
        throw (LcmException) ex.getCause();
      }
      if (ex.getCause() instanceof LcmValidationException) {
        throw (LcmValidationException) ex.getCause();
      }
    }

    return null;
  }

  private String getSupportedSourceType(Class<?> clazz) {
    try {
      BackendSource source = clazz.getAnnotation(BackendSource.class);

      if (source != null) {
        return source.type();
      }
    } catch (SecurityException ex) {
      logger.log(Level.WARNING, null, ex);
    }

    return null;
  }

}
