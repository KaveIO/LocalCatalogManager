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
package nl.kpmg.lcm.server.data.service;

import nl.kpmg.lcm.common.data.EnrichmentProperties;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.TaskSchedule;
import nl.kpmg.lcm.common.data.TaskType;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.data.dao.TaskScheduleDao;
import nl.kpmg.lcm.server.task.enrichment.DataEnrichmentTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shristov
 */
@Service
public class TaskScheduleService {
  @Autowired
  private StorageService storageService;

  @Autowired
  private MetaDataService metaDataService;

  @Autowired
  private TaskScheduleDao taskScheduleDao;

  public TaskSchedule save(TaskSchedule taskSchedule) {
    return taskScheduleDao.save(taskSchedule);
  }

  public TaskSchedule findFirstByOrderByIdDesc() {
    return taskScheduleDao.findFirstByOrderByIdDesc();
  }

  public TaskSchedule createNewScheduleWithEnrichmentJobs() {
    List<TaskSchedule.TaskScheduleItem> taskList = new ArrayList<TaskSchedule.TaskScheduleItem>();
    processStorages(taskList);

    processMetaDatas(taskList);
    TaskSchedule lastSchedule = findFirstByOrderByIdDesc();
    if (lastSchedule == null) {
      lastSchedule = new TaskSchedule();
    }

    lastSchedule.setEnrichmentItems(taskList);
    return save(lastSchedule);
  }

  private void processMetaDatas(List<TaskSchedule.TaskScheduleItem> taskList) {
    List<MetaData> metadataList = metaDataService.findAll();
    for (MetaData metadata : metadataList) {
      MetaDataWrapper metadataWrapper = new MetaDataWrapper(metadata);
      EnrichmentProperties enrichment =
          metadataWrapper.getEnrichmentProperties().getEnrichmentProperties();
      if (enrichment != null) {
        TaskSchedule.TaskScheduleItem item = new TaskSchedule.TaskScheduleItem();
        item.setCron(enrichment.getCronExpression());
        item.setName("Schedule for: " + metadataWrapper.getName());
        item.setTarget(metadataWrapper.getId());
        item.setTargetType(MetaData.class.getName());
        item.setJob(DataEnrichmentTask.class.getName());
        item.setTaskType(TaskType.ENRICHMENT);

        taskList.add(item);
      }
    }
  }

  private void processStorages(List<TaskSchedule.TaskScheduleItem> taskList) {
    List<Storage> storageList = storageService.findAll();
    for (Storage storage : storageList) {
      if (storage.getEnrichmentProperties() != null) {
        EnrichmentProperties enrichment =
            new EnrichmentProperties(storage.getEnrichmentProperties());
        TaskSchedule.TaskScheduleItem item = new TaskSchedule.TaskScheduleItem();
        item.setCron(enrichment.getCronExpression());
        item.setName("Schedule for: " + storage.getName());
        item.setTarget(storage.getId());
        item.setTargetType(Storage.class.getName());
        item.setJob(DataEnrichmentTask.class.getName());
        item.setTaskType(TaskType.ENRICHMENT);

        taskList.add(item);
      }
    }
  }

  public TaskSchedule createManagerSchedule(TaskSchedule.TaskScheduleItem taskScheduleItem) {
    TaskSchedule last = findFirstByOrderByIdDesc();

    if (last == null) {
      last = new TaskSchedule();
    }
    List<TaskSchedule.TaskScheduleItem> managerItem = new ArrayList();
    managerItem.add(taskScheduleItem);
    last.setManagerItems(managerItem);

    return save(last);
  }

  public void removeMetadataFromTaskSchedule(MetaData metaData) {

    MetaDataWrapper metaDataWrapper = new MetaDataWrapper(metaData);

    TaskSchedule lastSchedule = findFirstByOrderByIdDesc();
    if (lastSchedule == null) {
      return;
    }
    List<TaskSchedule.TaskScheduleItem> items = lastSchedule.getEnrichmentItems();

    for (TaskSchedule.TaskScheduleItem item : items) {
      if (metaDataWrapper.getId().equals(item.getTarget())) {
        items.remove(item);
        save(lastSchedule);
        // as metadata has unique id it must exist just once in the enrichment list.
        break;
      }
    }
  }

  public void removeStorageFromTaskSchedule(Storage storage) {

    if (storage == null) {
      throw new IllegalArgumentException("Storage could not be null!");
    }

    TaskSchedule lastSchedule = findFirstByOrderByIdDesc();
    if (lastSchedule == null) {
      return;
    }
    List<TaskSchedule.TaskScheduleItem> items = lastSchedule.getEnrichmentItems();

    for (TaskSchedule.TaskScheduleItem item : items) {
      if (storage.getId().equals(item.getTarget())) {
        items.remove(item);
        save(lastSchedule);
        // as storage has unique id it must exist just once in the enrichment list.
        break;
      }
    }
  }


}