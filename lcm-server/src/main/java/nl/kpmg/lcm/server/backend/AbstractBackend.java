/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
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

import nl.kpmg.lcm.server.data.ProgressIndicationFactory;
import nl.kpmg.lcm.server.data.metadata.DataItemsDescriptor;
import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.server.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.validation.Notification;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author mhoekstra
 */
abstract class AbstractBackend implements Backend {
  protected final MetaDataWrapper metaDataWrapper;
  protected ProgressIndicationFactory progressIndicationFactory;
  protected final StorageService storageService;

  private final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StorageService.class.getName());

  protected AbstractBackend(MetaData metaData, StorageService storageService) {
    metaDataWrapper = new MetaDataWrapper(metaData);
    Notification validationNotification = new Notification();
    validation(metaDataWrapper, validationNotification);
    if (storageService == null) {
      validationNotification.addError("Storage survice is null");
    }
    if (validationNotification.hasErrors()) {
      throw new LcmValidationException(validationNotification);
    }
    this.storageService = storageService;
  }

  private void validation(MetaDataWrapper metaDataWrapper, Notification notification) {
    if (metaDataWrapper.isEmpty()) {
      notification.addError("The metaData could not be null!", null);
      return;
    }

    List<String> uriList = metaDataWrapper.getData().getUri();
    for (String uri : uriList) {
      try {
        URI parsedUri = new URI(uri);
        if (!getSupportedUriSchema().contains(parsedUri.getScheme())) {
          notification
              .addError(
                  String
                      .format(
                          "Detected uri schema (%s) doesn't match with this backends supported uri schema (%s)",
                          parsedUri.getScheme(), getSupportedUriSchema()), null);
        }
      } catch (URISyntaxException ex) {
        notification.addError(
            String.format("Unable to parse URI (%s) ", metaDataWrapper.getData().getUri()), ex);
      }
    }
  }

  public Set<String> getSupportedUriSchema() {
    Set<String> supportedSchemas = new HashSet();
    BackendSource sourceAnnotation = this.getClass().getAnnotation(BackendSource.class);
    if (sourceAnnotation != null) {
      for (String type : sourceAnnotation.type()) {
        supportedSchemas.add(type);
      }
    }

    return supportedSchemas;
  }

  protected void expandDataURISection() {
    List<String> uriList = metaDataWrapper.getData().getUri();
    for (String uri : uriList) {
      if (!uri.contains("*")) {
        processDataItemURI(uri);
      } else {// something like 'csv://local/test*.csv'
        URI parsedUri = null;
        try {
          parsedUri = new URI(uri);
        } catch (URISyntaxException ex) {
          LOGGER.error("Error! URI can not be parsed:" + uri);
          continue;
        }
        String path = parsedUri.getPath();
        int index = StringUtils.lastIndexOf(path, "/");
        String subPath = path.substring(0, index);
        if (subPath.contains("*")) {
          LOGGER.error("Error! URI has invalid syntax. Wildcard symbol is used in the path:" + uri);
          continue;
        }

        String item = path.substring(index + 1, path.length());
        Pattern namePattern = Pattern.compile(item.replace("*", ".*"));
        String storageName =
          parsedUri.getHost() != null ? parsedUri.getHost() : parsedUri.getAuthority();
        List<String> dataItemList = loadDataItems(storageName, subPath);
        for (String dataItemName : dataItemList) {
          if (namePattern.matcher(dataItemName).matches()) {
            String uriItem = uri.replace(item, dataItemName);
            processDataItemURI(uriItem);
          }
        }
      }
    }
  }

  /**
   *
   * @param storage : storage name
   * @param subPath : sub path if applicable for the data type.
   * @return a list of all data items that persists in the give storage and sub directory(if any)
   *         Example 1 storage : local (local is file storage that points to /tmp directory) subPath
   *         : /lcm return : all the files located in /tmp/lcm will be returned
   *
   *         storage : local-hive (local-hive is hive storage that points to 'lcm' database) subPath
   *         : null - it will be ignored no matter of the value return : all the tables in 'lcm'
   *         database will be returned.
   *
   *         Note: subPath is not applicable for all dataTypes. For example hive storage could not
   *         have sub path.
   */
  protected abstract List<String> loadDataItems(String storageName, String subPath);

  private void processDataItemURI(String uri) {
    String key = getURIHasKey(uri);
    if (key == null) {
      key = generateKey();
      DataItemsDescriptor dynamicDataDescriptor =
          new DataItemsDescriptor(metaDataWrapper.getMetaData(), key);
      dynamicDataDescriptor.setURI(uri);
      metaDataWrapper.getDynamicData().addDynamicDataDescriptors(key,
          dynamicDataDescriptor.getMap());
    }
  }

  private String getURIHasKey(String uri) {
    Map<String, DataItemsDescriptor> descriptors =
        metaDataWrapper.getDynamicData().getAllDynamicDataDescriptors();
    if (descriptors == null) {
      return null;
    }

    for (String key : descriptors.keySet()) {
      if (descriptors.get(key).getURI() != null && descriptors.get(key).getURI().equals(uri)) {

        return key;
      }
    }
    return null;
  }

  private String generateKey() {
    Long now = System.currentTimeMillis();
    Object object = new Object();
    String[] addressArr = object.toString().split("@");
    String address = "";
    if (addressArr != null && addressArr.length > 1) {
      address = addressArr[1];
    }
    String key = "" + now + address;
    if (key.length() > 20) {
      key = key.substring(0, 20);
    }

    return key;
  }

  @Override
  public void setProgressIndicationFactory(ProgressIndicationFactory progressIndicationFactory) {
    this.progressIndicationFactory = progressIndicationFactory;
  }

}
