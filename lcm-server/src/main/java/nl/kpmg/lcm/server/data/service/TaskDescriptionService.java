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

import nl.kpmg.lcm.common.data.DataState;
import nl.kpmg.lcm.common.data.ProgressIndication;
import nl.kpmg.lcm.common.data.TaskDescription;
import nl.kpmg.lcm.common.data.TaskType;
import nl.kpmg.lcm.common.data.metadata.DataItemsDescriptor;
import nl.kpmg.lcm.common.data.metadata.DynamicDataDescriptor;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.cron.job.processor.AtlasDataExecutor;
import nl.kpmg.lcm.server.cron.job.processor.DataDeletionExecutor;
import nl.kpmg.lcm.server.data.dao.TaskDescriptionDao;
import nl.kpmg.lcm.server.integration.atlas.TransformationException;
import nl.kpmg.lcm.server.integration.service.AtlasMetadataService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mhoekstra
 */
@Service
public class TaskDescriptionService {
  private final Logger LOGGER = LoggerFactory.getLogger(TaskDescriptionService.class.getName());

  @Autowired
  private TaskDescriptionDao taskDescriptionDao;

  @Autowired
  private MetaDataService metadataService;

  @Autowired
  private AtlasMetadataService atlasMetadataService;

  public List<TaskDescription> findAll() {
    List result = Lists.newArrayList(taskDescriptionDao.findAll());
    return result;
  }

  public List<TaskDescription> findByType(TaskType type) {
    Sort sort = new Sort(new Order(Direction.DESC, "startTime"));
    List result = Lists.newArrayList(taskDescriptionDao.findByType(type, sort));
    return result;
  }

  public List<TaskDescription> findByType(TaskType type, int limit) {
    Sort sort = new Sort(new Order(Direction.DESC, "startTime"));
    Pageable pageable = new PageRequest(0, limit, sort);
    List result = Lists.newArrayList(taskDescriptionDao.findByType(type, pageable));
    return result;
  }

  public List<TaskDescription> findByTypeAndStatus(TaskType type, TaskDescription.TaskStatus status) {
    Sort sort = new Sort(new Order(Direction.DESC, "startTime"));
    List result = Lists.newArrayList(taskDescriptionDao.findByTypeAndStatus(type, status, sort));
    return result;
  }

  public List<TaskDescription> findByTypeAndStatus(TaskType type,
          TaskDescription.TaskStatus status, int limit) {
    Sort sort = new Sort(new Order(Direction.DESC, "startTime"));
    Pageable pageable = new PageRequest(0, limit, sort);
    List result = Lists.newArrayList(taskDescriptionDao.findByTypeAndStatus(type, status, pageable));
    return result;
  }
  public List<TaskDescription> find(int limit) {
    Sort sort = new Sort(new Order(Direction.DESC, "startTime"));
    Pageable pageable = new PageRequest(0, limit, sort);
    Page<TaskDescription> page = taskDescriptionDao.findAll(pageable);
    return Lists.newArrayList(page);
  }

  public TaskDescription findOne(String taskId) {
    return taskDescriptionDao.findOne(taskId);
  }

  public void updateProgress(String taskId, ProgressIndication indication) {
    TaskDescription description = taskDescriptionDao.findOne(taskId);
    description.getProgress().add(indication);
    taskDescriptionDao.save(description);
  }

  public List<TaskDescription> findByStatus(TaskDescription.TaskStatus status) {
    return taskDescriptionDao.findByStatus(status);
  }

  public List<TaskDescription> findByStatus(TaskDescription.TaskStatus status, int limit) {
    Sort sort = new Sort(new Order(Direction.DESC, "startTime"));
    Pageable pageable = new PageRequest(0, limit, sort);
    return taskDescriptionDao.findByStatus(status, pageable);
  }

  public void markTaskAsRunning(TaskDescription description) {
    if (description == null) {
      return;
    }
    description.setStatus(TaskDescription.TaskStatus.RUNNING);
    description.setStartTime(new Date());
    taskDescriptionDao.save(description);
  }

  public TaskDescription markTaskAsFinished(String taskId, TaskDescription.TaskStatus status) {
    TaskDescription description = taskDescriptionDao.findOne(taskId);
    description.setStatus(status);
    description.setEndTime(new Date());
    TaskDescription saved = taskDescriptionDao.save(description);

    return saved;
  }

  public TaskDescription updateStatus(String taskId, TaskDescription.TaskStatus status) {
    TaskDescription description = taskDescriptionDao.findOne(taskId);
    description.setStatus(status);
    TaskDescription saved = taskDescriptionDao.save(description);

    return saved;
  }

  public TaskDescription updateOptions(String taskId, Map<String, String> options) {
    TaskDescription description = taskDescriptionDao.findOne(taskId);
    description.setOptions(options);
    TaskDescription saved = taskDescriptionDao.save(description);

    return saved;
  }

  public TaskDescription createNew(TaskDescription description) {
    TaskDescription inserted = taskDescriptionDao.save(description);
    return inserted;
  }

  public void delete(String taskId) {
    taskDescriptionDao.delete(taskId);
  }

  public void deleteAll() {
    taskDescriptionDao.deleteAll();
  }

  public boolean existSuchUnfinishedTaskDescription(TaskType type, String target) {
    List<TaskDescription> list = findByType(type);
    for (TaskDescription task : list) {
      if (task.getTarget().equals(target)) {
        if (task.getStatus().equals(TaskDescription.TaskStatus.PENDING)
            || task.getStatus().equals(TaskDescription.TaskStatus.SCHEDULED)
            || task.getStatus().equals(TaskDescription.TaskStatus.RUNNING)) {
          return true;
        }
      }
    }
    return false;
  }

  public void createNewDataDeletionTaskDescriptions() {
    List<MetaData> metadataList = metadataService.findAll();
    for (MetaData metadata : metadataList) {
      MetaDataWrapper metadataWrapper = new MetaDataWrapper(metadata);
      String executionExpirationTime =
          metadataWrapper.getExpirationTime().getExecutionExpirationTime();

      if(!isMetadataExpirationTimeInThePast(executionExpirationTime)){
          continue;
      }

      if (existSuchUnfinishedTaskDescription(TaskType.DELETE, metadataWrapper.getId())) {
        continue;
      }

     DynamicDataDescriptor dynamicDataDescriptor = metadataWrapper.getDynamicData();
     if (!isAnyDataAttached(dynamicDataDescriptor)){
         continue;
     }

     String metadataId = metadataWrapper.getId();
        createNewTask(DataDeletionExecutor.class.getName(), TaskType.DELETE, metadataId);
    }
  }

  private long convertTimestampSecondsToMiliseconds(String timestamp) {
    return Long.parseLong(timestamp) * 1000;
  }
  
  private boolean isMetadataExpirationTimeInThePast(String executionExpirationTime){
      if (executionExpirationTime == null || executionExpirationTime.equals("")) {
        return false;
      }

      Date currentDate = new Date();
      long currentExpirationTimeInMiliseconds = currentDate.getTime();
      long metadataExpirationTimeInMiliseconds =
          convertTimestampSecondsToMiliseconds(executionExpirationTime);

      if (metadataExpirationTimeInMiliseconds > currentExpirationTimeInMiliseconds) {
        return false;
      }
      return true;
  }
  
  private boolean isAnyDataAttached(DynamicDataDescriptor dynamicDataDescriptor){
       if (dynamicDataDescriptor.getAllDynamicDataDescriptors() == null) {
        return false;
      }

      for (String key : dynamicDataDescriptor.getAllDynamicDataDescriptors().keySet()) {
        DataItemsDescriptor dynamicItemDescriptor =
            dynamicDataDescriptor.getDynamicDataDescriptor(key);
        if (dynamicItemDescriptor.getDetailsDescriptor().getState().equals(DataState.ATTACHED)) {
          return true;
        }

      }
    return false;
  }

  public void createAtlasTasks() {
    createAtlasMetaDataUpdateAndDeleteTasks();
    createAtlasMetaDataInsertionTasks();
  }

  private void createAtlasMetaDataUpdateAndDeleteTasks() {
    List<MetaData> lcmMetadatas = metadataService.findAll();
    for (MetaData lcmMetadata : lcmMetadatas) {
      MetaDataWrapper lcmMetadataWrapper = new MetaDataWrapper(lcmMetadata);
      String guid = lcmMetadataWrapper.getAtlasMetadata().getGuid();
      if (guid == null) {
        continue; // this is not atlas metadata and skip it
      }

      try {
        MetaData atlasMetadata = atlasMetadataService.getOne(guid);

        if (atlasMetadata == null) {
          LOGGER.warn("Atlas metadata with guid: " + guid + " is null.");
          continue;
        }

        MetaDataWrapper atlasMetadataWrapper = new MetaDataWrapper(atlasMetadata);

        // Delete atlas metadata from LCM if it no longer exists in Apache Atlas.
        if (atlasMetadataWrapper.getAtlasMetadata().getStatus().equals("DELETED")
            && !existSuchUnfinishedTaskDescription(TaskType.ATLAS_DELETE, lcmMetadata.getId())) {
          createNewTask(AtlasDataExecutor.class.getName(), TaskType.ATLAS_DELETE,
              lcmMetadata.getId());
          continue;
        }

        String lcmMetadataLastModifiedTime =
            lcmMetadataWrapper.getAtlasMetadata().getLastModifiedTime();
        String atlasMetadataLastModifiedTime =
            atlasMetadataWrapper.getAtlasMetadata().getLastModifiedTime();

        // Update atlas metadata from LCM if its structure is already changed in Apache Atlas.
        if (isAtlasMetadataUpdated(lcmMetadataLastModifiedTime, atlasMetadataLastModifiedTime)
            && !existSuchUnfinishedTaskDescription(TaskType.ATLAS_UPDATE, lcmMetadata.getId())) {
          createNewTask(AtlasDataExecutor.class.getName(), TaskType.ATLAS_UPDATE,
              lcmMetadata.getId());
        }

      } catch (TransformationException ex) {
        LOGGER.error("Unable to get the atlas metadata with guid: " + guid + ". Error message: "
            + ex.getMessage());
      }
    }
  }

  private boolean isAtlasMetadataUpdated(String lcmMetadataLastModifiedTime,
      String atlasMetadataLastModifiedTime) {
    return atlasMetadataLastModifiedTime != null
        && !atlasMetadataLastModifiedTime.equals(lcmMetadataLastModifiedTime);
  }

  private void createAtlasMetaDataInsertionTasks() {
    List<MetaData> atlasMetadatas = atlasMetadataService.getAll();

    for (MetaData atlasMetadata : atlasMetadatas) {
      MetaDataWrapper atlasMetadataWrapper = new MetaDataWrapper(atlasMetadata);

      if (atlasMetadataWrapper.getAtlasMetadata().getStatus().equals("DELETED")) {
        continue;
      }

      String atlasMetadataGuid = atlasMetadataWrapper.getAtlasMetadata().getGuid();

      List<MetaData> lcmMetadatas = metadataService.findAll();
      boolean doExist = false;
      for (MetaData lcmMetadata : lcmMetadatas) {
        MetaDataWrapper lcmMetadataWrapper = new MetaDataWrapper(lcmMetadata);
        String lcmMetadataGuid = lcmMetadataWrapper.getAtlasMetadata().getGuid();

        if (atlasMetadataGuid.equals(lcmMetadataGuid)) {
          doExist = true;
          break;
        }
      }

      if (!doExist) {
        if (existSuchUnfinishedTaskDescription(TaskType.ATLAS_INSERT, atlasMetadataGuid)) {
          continue;
        }

        createNewTask(AtlasDataExecutor.class.getName(), TaskType.ATLAS_INSERT,
            atlasMetadataGuid);
      }
    }
  }

  private void createNewTask(String job, TaskType type, String metadataId) {
    TaskDescription task = new TaskDescription();
    task.setJob(job);
    task.setType(type);
    task.setStatus(TaskDescription.TaskStatus.PENDING);
    task.setTarget(metadataId);

    if (!type.equals(TaskType.DELETE)) {
      Map<String, String> map = new HashMap<String, String>();
      map.put("type", type.name());
      task.setOptions(map);
    }

    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, 2);
    Date startTime = calendar.getTime();
    task.setStartTime(startTime);

    createNew(task);
  }
}