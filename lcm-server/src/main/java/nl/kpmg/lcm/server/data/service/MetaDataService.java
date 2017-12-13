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

import nl.kpmg.lcm.common.Constants;
import nl.kpmg.lcm.common.data.EnrichmentProperties;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author mhoekstra
 */
@Service
public class MetaDataService {
  private final Logger LOGGER = LoggerFactory.getLogger(MetaDataService.class.getName());
  public static final int MAX_EXPIRATION_YEAR_DURATION = 50;

  @Autowired
  private MetaDataDao metaDataDao;

  @Autowired
  private StorageService storageService;

  @Autowired
  private TaskScheduleService taskScheduleService;

  public List<MetaData> findAll() {
    return Lists.newLinkedList(metaDataDao.findAllByInactive(null));
  }

  /**
   *
   * @return return all the records in the collection including the deleted
   */
  public List<MetaData> findAllRecords() {
    return Lists.newLinkedList(metaDataDao.findAll());
  }

  /**
   *
   * @param id id of the metadata
   * @return only active metadata.
   */
  public MetaData findById(String id) {
    return metaDataDao.findOneByIdAndInactive(id, null);
  }

  /**
   *
   * @param id id of the metadata
   * @return return metadata record no matter if it is inactivated or not.
   */
  public MetaData findRecordById(String id) {
    return metaDataDao.findOne(id);
  }


  public void create(MetaData metadata) {
    MetaDataWrapper wrapper = new MetaDataWrapper(metadata);
    String metadataExecutionTime = wrapper.getExpirationTime().getExecutionExpirationTime();
    String metadataTransferTime = wrapper.getExpirationTime().getTransferExpirationTime();

    if (metadataExecutionTime != null && metadataTransferTime != null) {
      long execTime = getExecutionExpirationTime(metadataExecutionTime);
      long transferTime = getTransferExpirationTime(metadataTransferTime);

      if (!isExpirationTimeInTheFuture(execTime) || !isExpirationTimeInTheFuture(transferTime)) {
        throw new LcmValidationException("Can`t set an expiration time that had already passed.");
      }
    }
    metaDataDao.save(metadata);
  }

  public void update(MetaData metadata) {
    MetaData oldMetadata = metaDataDao.findOne(metadata.getId());
    if (oldMetadata == null) {
      throw new LcmValidationException("Can`t update metadata which doesn`t exist.");
    }

    MetaDataWrapper oldMetadataWrapper = new MetaDataWrapper(oldMetadata);
    MetaDataWrapper updatedMetadataWrapper = new MetaDataWrapper(metadata);

    String oldExecutionTime = oldMetadataWrapper.getExpirationTime().getExecutionExpirationTime();
    String updatedExecutionTime =
        updatedMetadataWrapper.getExpirationTime().getExecutionExpirationTime();

    if (oldExecutionTime != null) {
      if (!oldExecutionTime.equals(updatedExecutionTime)) {
        throw new LcmValidationException(
            "Once being set the execution expiration time could not be changed or removed.");
      }
    }

    String updatedTransferTime =
        updatedMetadataWrapper.getExpirationTime().getTransferExpirationTime();
    if (updatedExecutionTime != null && updatedTransferTime != null) {
      long execTime = getExecutionExpirationTime(updatedExecutionTime);
      long transferTime = getTransferExpirationTime(updatedTransferTime);

      if (!isExpirationTimeInTheFuture(execTime) || !isExpirationTimeInTheFuture(transferTime)) {
        throw new LcmValidationException("Can`t set an expiration time that had already passed.");
      }
    }
    metaDataDao.save(metadata);
  }

  public void delete(MetaData metadata) {
    taskScheduleService.removeMetadataFromTaskSchedule(metadata);
    metadata.setInactive(String.valueOf(System.currentTimeMillis()));
    metaDataDao.save(metadata);
  }

  private long getExecutionExpirationTime(String executionTime) {
    return convertTimestampSecondsToMiliseconds(executionTime);
  }

  private long getTransferExpirationTime(String transferTime) {
    return convertTimestampSecondsToMiliseconds(transferTime);
  }

  private boolean isExpirationTimeInTheFuture(long expirationTimeInMiliseconds) {
    Date currentDate = new Date();
    Calendar currentCal = Calendar.getInstance();
    int currentYear = currentCal.get(Calendar.YEAR);

    long currentExpirationTimeInMiliseconds = currentDate.getTime();

    Date timestampDate = new Date(expirationTimeInMiliseconds);
    Calendar timestampCal = Calendar.getInstance();
    timestampCal.setTime(timestampDate);
    int timestampYear = timestampCal.get(Calendar.YEAR);

    if (timestampYear > currentYear + MAX_EXPIRATION_YEAR_DURATION) {
      LOGGER.warn("The expiration time of the metadata is too late in the future: "
          + expirationTimeInMiliseconds + ".");
      return false;
    } else if (expirationTimeInMiliseconds <= currentExpirationTimeInMiliseconds) {
      LOGGER.warn("The expiration time of the metadata has already passed: "
          + expirationTimeInMiliseconds + ".");
      return false;
    }
    return true;
  }

  private long convertTimestampSecondsToMiliseconds(String timestamp) {
    return Long.parseLong(timestamp) * 1000;
  }

  public List<MetaData> findByStorageName(String storageName) {
    List<MetaData> metadataList = findAll();
    HashMap<String, MetaData> targets = new HashMap();
    for (MetaData metadata : metadataList) {
      List<String> storages = new MetaDataWrapper(metadata).getStorageName();
      for (String storage : storages) {
        if (storage.equals(storageName)) {
          targets.put(metadata.getId(), metadata);
          break; // Once this metadata is added it is not needed to ad it again
        }
      }
    }

    return new ArrayList(targets.values());
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
        return false;
      } catch (LcmException ex) {
        LOGGER.info("Unable to get metadata info. Error: " + ex.getMessage());
        return false;
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

  /**
   * 
   * @param namespace base namespace path
   * @param recursive when true returns and the metadatas of sub namespaces.
   * @return List of metadatas that belongs to @namespace
   */
  public List<MetaData> findAllByNamespace(String namespace, boolean recursive) {
    List<MetaData> all = Lists.newLinkedList(metaDataDao.findAll());
    List<MetaData> filtered = new LinkedList();
    for (MetaData metadata : all) {
      if (doesMetadataBelongsToNamespace(metadata, namespace, recursive)) {
        filtered.add(metadata);
      }
    }

    return filtered;
  }

  /**
   * 
   * @param metadata which is check
   * @param namespace must be in format "section1/section2"
   * @param recursive check recursively i.e. if the metadata belongs to subsection
   * @return true if @metadata belongs to @namespace
   */
  public boolean doesMetadataBelongsToNamespace(MetaData metadata, String namespace,
      boolean recursive) {
    MetaDataWrapper metadataWrapper = new MetaDataWrapper(metadata);
    String path = metadataWrapper.getData().getPath();

    if (path != null && path.equals(namespace)) {
      return true;
    }

    // ensure that the rest of the path is separate namespace section
    // i.e. namepsace kpmg/lcm/test belongs to kpmg/lcm
    // but kpmg/lsmtest does not!
    namespace += Constants.NAMESPACE_SEPARATOR;
    if (path != null && recursive && path.startsWith(namespace)) {
      return true;
    }

    return false;
  }

  /**
   * 
   * @param namespace must be in format "section1/section2".
   * @param recursive check recursively i.e. for subsections of the subsection
   * @return List with sub namespaces.
   */
  public Set<String> getAllSubNamespaces(String namespace, boolean recursive) {
    List<MetaData> all = Lists.newLinkedList(findAll());
    Set<String> namespaceSet = new HashSet();
    for (MetaData metadata : all) {
      MetaDataWrapper metadataWrapper = new MetaDataWrapper(metadata);
      String path = metadataWrapper.getData().getPath();
      if (path == null) {
        LOGGER.warn("Metadata has null path:  Id: " + metadata.getId());
        continue;
      }

      if (path.equals(namespace)) {
        namespaceSet.add(path);
        continue;
      }

      // ensure that the rest of the path is separate namespace section
      // i.e. namepsace kpmg/lcm/test belongs to kpmg/lcm
      // but kpmg/lsmtest does not!
      namespace += Constants.NAMESPACE_SEPARATOR;
      if (recursive && path.startsWith(namespace)) {
        namespaceSet.add(path);
      }

    }
    return namespaceSet;
  }

  public void removeAll() {
    List<MetaData> all = Lists.newLinkedList(metaDataDao.findAllByInactive(null));
    for (MetaData md : all) {
      delete(md);
    }
  }
}
