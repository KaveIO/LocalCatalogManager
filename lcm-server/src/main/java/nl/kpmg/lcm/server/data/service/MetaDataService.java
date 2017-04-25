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
import nl.kpmg.lcm.server.data.EnrichmentProperties;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;
import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.server.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.exception.LcmException;
import nl.kpmg.lcm.server.exception.LcmValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author mhoekstra
 */
@Service
public class MetaDataService {
  private final Logger LOGGER = LoggerFactory.getLogger(MetaDataService.class.getName());

  @Autowired
  private MetaDataDao metaDataDao;

  @Autowired
  private StorageService storageService;

  public List<MetaData> findAll() {
    return Lists.newLinkedList(metaDataDao.findAll());
  }

  public MetaData findById(String id) {
    return metaDataDao.findOne(id);
  }

  public void create(MetaData metadata) {
    metaDataDao.save(metadata);
  }

  public void update(MetaData metadata) {
    metaDataDao.save(metadata);
  }

  public void delete(MetaData metadata) {
    metaDataDao.delete(metadata);
  }

  public List<MetaData> findByStorageName(String storageName) {
    // TODO This probably could be optimized.
    List<MetaData> metadataList = findAll();
    List<MetaData> targets = new LinkedList();
    for (MetaData metadata : metadataList) {
      if (new MetaDataWrapper(metadata).getStorageName().equals(storageName)) {
        targets.add(metadata);
      }
    }

    return targets;
  }

  /**
   * Scan data and update the metadata with actual data properties as: data size, accessibility,
   * current data structure etc.
   * 
   * @param metaDataId
   * @return true if the enrichment was successful.
   */
  public boolean enrichMetadata(MetaDataWrapper metadataWrapper, EnrichmentProperties enrichment) {
    Backend backend = null;
    try {
      try {
        backend = storageService.getBackend(metadataWrapper);
        if (backend == null) {
          return false;
        }
      } catch (LcmValidationException ex) {
        LOGGER.warn(ex.getNotification().errorMessage());
        return false;
      } catch (LcmException ex) {
        LOGGER.warn(ex.getMessage());
        return false;
      }

      MetaData updatedMetaData = null;
      try {
        updatedMetaData = backend.enrichMetadata(enrichment);
      } catch (LcmValidationException ex) {
        LOGGER.info("Unable to get metadata info. Error: " + ex.getNotification().errorMessage());
      } catch (LcmException ex) {
        LOGGER.info("Unable to get metadata info. Error: " + ex.getMessage());
      }
      if (updatedMetaData != null) {
        update(updatedMetaData);
      }
    } finally {
      if (backend != null) {
        backend.free();
      }
    }

    return true;
  }
}