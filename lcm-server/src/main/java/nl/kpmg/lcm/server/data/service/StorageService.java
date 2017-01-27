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

import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendFactory;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.dao.StorageDao;
import nl.kpmg.lcm.server.data.meatadata.MetaDataWrapper;
import nl.kpmg.lcm.server.exception.LcmException;

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

  public StorageDao getStorageDao() {
    return storageDao;
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
    if (metadataWrapper == null || metadataWrapper.isEmpty() || metadataWrapper.getDataUri() == null
        || metadataWrapper.getDataUri().isEmpty()) {
      String errorMessage =
          "Invalid input data! Metata data could not be null nither the data URI!";
      LOGGER.warn(errorMessage);

      throw new LcmException(errorMessage);
    }

    try {
      URI parsedUri = new URI(metadataWrapper.getDataUri());
      String scheme = parsedUri.getScheme();

      String storageName =
          parsedUri.getHost() != null ? parsedUri.getHost() : parsedUri.getAuthority();
      Storage storage = storageDao.findOneByName(storageName);

      if (storage == null) {
        throw new LcmException(
            "Error! Unable to find Storage for the given metadata! Storage name:" + storageName);
      }

      Backend backend = backendFactory.createBackend(scheme, storage, metadataWrapper);

      return backend;

    } catch (URISyntaxException ex) {
      LOGGER.error( null, ex);
      throw new LcmException("Error! Unable to parse medata data URI!");
    }
  }
}
