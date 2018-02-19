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

package nl.kpmg.lcm.server.data.service;

import jersey.repackaged.com.google.common.collect.Lists;

import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.TestResult;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendFactory;
import nl.kpmg.lcm.server.backend.storage.AzureStorage;
import nl.kpmg.lcm.server.backend.storage.HiveStorage;
import nl.kpmg.lcm.server.backend.storage.MongoStorage;
import nl.kpmg.lcm.server.backend.storage.S3FileStorage;
import nl.kpmg.lcm.server.backend.storage.StorageTester;
import nl.kpmg.lcm.server.data.dao.StorageDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Crude service to work with backends.
 *
 * @author mhoekstra
 */
@Service
public class StorageService {
  private final Logger LOGGER = LoggerFactory.getLogger(StorageService.class.getName());

  @Autowired
  private StorageDao storageDao;

  @Autowired
  private BackendFactory backendFactory;

  public List<Storage> findAll() {
    return Lists.newLinkedList(storageDao.findAll());
  }

  public Storage findById(String id) {
    return storageDao.findOne(id);
  }

  public Storage findByName(String name) {
    return storageDao.findOneByName(name);
  }

  public void delete(Storage storage) {
    storageDao.delete(storage);
  }

  public Storage add(Storage storage) {
    return saveStorage(storage);
  }

  public Storage update(Storage storage) {
    return saveStorage(storage);
  }

  /**
   * Save the passed storage. If "credentials" section is missing then checks the mong if storage
   * with the same id already exists(i.e. update). If such exists the "credentials" section is
   * preserved. This is needed because the "credentials" section is never returned outside of server
   * module for security reasons. When the end user tries to update a storage it is important to
   * preserve the old credentials if new ones are not passed. There is one important consequence
   * from the statements above: "Credentials" sections can never be removed from existing storage.
   * If you want bypass it then set to storage empty "credentials" section.
   *
   * @param storage to save
   * @return saved storage
   */
  public Storage saveStorage(Storage storage) {
    if (storage.getType().equals(DataFormat.HIVE)) {
      storage = presetCredentails(storage);
      new HiveStorage(storage);// create not used wrapper to validate the structure of the storage
    } else if (storage.getType().equals(DataFormat.S3FILE)) {
      storage = presetCredentails(storage);
      new S3FileStorage(storage);// create not used wrapper to validate the structure of the storage
    } else if (storage.getType().equals(DataFormat.MONGO)) {
      storage = presetCredentails(storage);
      new MongoStorage(storage);// create not used wrapper to validate the structure of the storage
    } else if (AzureStorage.getSupportedStorageTypes().contains(storage.getType())) {
      storage = presetCredentails(storage);
      new AzureStorage(storage);// create not used wrapper to validate the structure of the storage
    }

    return storageDao.save(storage);
  }

  private Storage presetCredentails(Storage storage) {
    if (storage.getCredentials() == null) {
      Storage oldStorage = storageDao.findOne(storage.getId());
      storage.setCredentials(oldStorage.getCredentials());
    }

    return storage;
  }

  /**
   * Get a storage backend based on a MetaDataWrapper object.
   *
   * This will use the dataUri to retrieve the appropriate Backend.
   *
   * @param metadataWrapper of which the dataUri is used
   * @return the requested backend
   */
  public final Backend getBackend(final MetaDataWrapper metadataWrapper) {
    if (metadataWrapper == null || metadataWrapper.isEmpty()
        || metadataWrapper.getData().getUri() == null
        || metadataWrapper.getData().getUri().isEmpty()) {
      String errorMessage =
          "Invalid input data! Metadata data could not be null nither the data URI!";

      throw new LcmException(errorMessage);
    }

    try {
      // all the URIs must have same data type
      String unparesURI = metadataWrapper.getData().getUri().get(0);
      URI parsedUri = new URI(unparesURI);
      String scheme = parsedUri.getScheme();

      String storageName =
          parsedUri.getHost() != null ? parsedUri.getHost() : parsedUri.getAuthority();
      Storage storage = storageDao.findOneByName(storageName);

      if (storage == null) {
        throw new LcmException(
            "Error! Unable to find Storage for the given metadata! Storage name:" + storageName);
      }

      Backend backend = backendFactory.createBackend(scheme, this, metadataWrapper);

      return backend;
    } catch (URISyntaxException ex) {
      throw new LcmException("Error! Unable to parse medata data URI!");
    }
  }

  public String getStorageItemName(String uri) {
    String result = null;
    try {
      if (uri != null) {
        URI parsedUri = new URI(uri);
        result = parsedUri.getPath();
      }
    } catch (URISyntaxException ex) {
      LOGGER.warn("unable to parse storage uri: " + uri);
      result = null;
    }
    return result;
  }

  public Storage getStorageByUri(String uri) {
    String storagName = getStorageName(uri);
    return findByName(storagName);
  }

  public String getStorageName(String uri) {
    String result = null;
    try {
      if (uri != null) {
        URI parsedUri = new URI(uri);
        result = parsedUri.getHost() != null ? parsedUri.getHost() : parsedUri.getAuthority();
      }
    } catch (URISyntaxException ex) {
      LOGGER.warn("unable to parse storage uri: " + uri);
      result = null;
    }
    return result;
  }

  public TestResult testStorage(String id) {
    StorageTester tester = new StorageTester();
    Storage storage = findById(id);
    if (storage == null) {
      throw new LcmException("Error! Unable to find Storage for the given metadata! Storage id:"
          + id);
    }
    TestResult result = tester.testAccessability(storage);
    storage.setStatus(result.getCode() + ":" + result.getMessage());

    saveStorage(storage);

    return result;
  }
}
