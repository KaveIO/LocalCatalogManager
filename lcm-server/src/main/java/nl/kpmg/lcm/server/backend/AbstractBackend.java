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

import nl.kpmg.lcm.common.data.EnrichmentProperties;
import nl.kpmg.lcm.common.data.metadata.DataItemsDescriptor;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.common.validation.Notification;
import nl.kpmg.lcm.server.data.ProgressIndicationFactory;
import nl.kpmg.lcm.server.data.service.StorageService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
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

  @Override
  public MetaData enrichMetadata(EnrichmentProperties properties) {
    expandDataURISection();
    if (metaDataWrapper.getDynamicData().getAllDynamicDataDescriptors() == null) {
      return metaDataWrapper.getMetaData();
    }

    for (String key : metaDataWrapper.getDynamicData().getAllDynamicDataDescriptors().keySet()) {
      DataItemsDescriptor dynamicDataDescriptor =
          metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key);
      long start = System.currentTimeMillis();

      String uri = dynamicDataDescriptor.getURI();
      if (!match(uri)) {
        metaDataWrapper.getDynamicData().removeDynamicDataItem(key);
        continue;
      }

      try {
        dynamicDataDescriptor.clearDetailsDescriptor();
        enrichMetadataItem(properties, key);
      } catch (Exception ex) {
        String message =
            "Unable to enrich medatadata : " + metaDataWrapper.getId() + " key: " + key
                + ". Error Message: " + ex.getMessage();
        LOGGER.error(message);
        throw new LcmException(message);
      } finally {
        dynamicDataDescriptor.getDetailsDescriptor().setUpdateTimestamp(new Date().getTime());
        long end = System.currentTimeMillis();
        dynamicDataDescriptor.getDetailsDescriptor().setUpdateDurationTimestamp(end - start);
      }
    }

    // in case that there are dynami data items exists.
    return metaDataWrapper.getMetaData();
  }

  protected abstract void enrichMetadataItem(EnrichmentProperties properties, String itemKey)
      throws IOException;

  protected void expandDataURISection() {
    List<String> uriList = metaDataWrapper.getData().getUri();
    for (String uri : uriList) {
      if (!uri.contains("*")) {
        processDataItemURI(uri);
      } else {
        List<String> dataItemList = getDataItems(uri);
        if (dataItemList == null) {
          continue;
        }

        processDataItemList(uri, dataItemList);
      }
    }
  }

  private void processDataItemList(String uri, List<String> dataItemList) {
    URI parsedUri = parseUri(uri);
    String path = processPath(parsedUri);
    int index = StringUtils.lastIndexOf(path, "/");
    String item = path.substring(index + 1, path.length());
    Pattern namePattern = Pattern.compile(item.replace("*", ".*"));
    for (String dataItemName : dataItemList) {
      if (namePattern.matcher(dataItemName).matches()) {
        String uriItem = uri.replace(item, dataItemName);
        processDataItemURI(uriItem);
      }
    }
  }

  private URI parseUri(String uri) {
    // something like 'csv://local/test*.csv'
    URI parsedUri = null;
    try {
      parsedUri = new URI(uri);
    } catch (URISyntaxException ex) {
      LOGGER.error("Error! URI can not be parsed:" + uri);
      return null;
    }
    return parsedUri;
  }

  private String processPath(URI parsedUri) {
    String path = parsedUri.getPath();
    // remove '/' in front of the path
    if (path.charAt(0) == '/') {
      path = path.substring(1);
    }
    return path;
  }

  private List getDataItems(String uri) {
    URI parsedUri = parseUri(uri);

    if (parsedUri == null) {
      return null;
    }

    String path = processPath(parsedUri);
    String subPath = "";
    int index = StringUtils.lastIndexOf(path, "/");
    if (index != -1) {
      subPath = path.substring(0, index);
    }
    if (subPath.contains("*")) {
      LOGGER.error("Error! URI has invalid syntax. Wildcard symbol is used in the path:"
          + parsedUri.toString());
      return null;
    }

    String storageName =
        parsedUri.getHost() != null ? parsedUri.getHost() : parsedUri.getAuthority();

    try {
      return loadDataItems(storageName, subPath);
    } catch (Exception ex) {
      LOGGER.warn("Unable to load data items for storage: " + storageName + " and subPath: "
          + subPath + ". Error message: " + ex.getMessage());
      return null;
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

  protected String getStorageName(String key) {
    DataItemsDescriptor dynamicDataDescriptor =
        metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key);
    String unparesURI = dynamicDataDescriptor.getURI();
    URI parsedUri;
    try {
      parsedUri = new URI(unparesURI);
    } catch (URISyntaxException ex) {
      LOGGER.error("unable to parse uri " + unparesURI + " for key:" + key);
      return null;
    }

    String storageName =
        parsedUri.getHost() != null ? parsedUri.getHost() : parsedUri.getAuthority();

    return storageName;
  }

  protected String getFilePath(String key) {
    DataItemsDescriptor dynamicDataDescriptor =
        metaDataWrapper.getDynamicData().getDynamicDataDescriptor(key);
    String unparesURI = dynamicDataDescriptor.getURI();
    URI parsedUri;
    try {
      parsedUri = new URI(unparesURI);
    } catch (URISyntaxException ex) {
      LOGGER.error("unable to parse uri " + unparesURI + " for key:" + key);
      return null;
    }

    return parsedUri.getPath();

  }

  protected boolean match(String uri) {
    List<String> dataUriList = metaDataWrapper.getData().getUri();
    for (String dataUri : dataUriList) {

      if (!dataUri.contains("*")) {
        if (dataUri.equals(uri)) {
          return true;
        }
      } else {
        Pattern dataUriPattern = Pattern.compile(dataUri.replace("*", ".*"));
        if (dataUriPattern.matcher(uri).matches()) {
          return true;
        }
      }
    }
    return false;
  }
}
