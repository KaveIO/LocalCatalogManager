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

import nl.kpmg.lcm.common.data.ProgressIndication;
import nl.kpmg.lcm.common.data.TaskDescription;
import nl.kpmg.lcm.common.data.TaskType;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.cron.job.processor.DataDeletionExecutor;
import nl.kpmg.lcm.server.data.dao.TaskDescriptionDao;

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

  public boolean doesExistTypeAndTarget(TaskType type, String target) {
    List<TaskDescription> list = findByType(type);
    for (TaskDescription task : list) {
      if (task.getTarget().equals(target))
        return true;
    }
    return false;
  }


  public void createNewDataDeletionTaskDescriptions() {
    List<MetaData> metadataList = metadataService.findAll();
    for (MetaData metadata : metadataList) {
      MetaDataWrapper metadataWrapper = new MetaDataWrapper(metadata);
      String executionExpirationTime =
          metadataWrapper.getExpirationTime().getExecutionExpirationTime();
      if (executionExpirationTime == null || executionExpirationTime.equals("")) {
        continue;
      }

      Date currentDate = new Date();
      long currentExpirationTimeInMiliseconds = currentDate.getTime();
      long metadataExpirationTimeInMiliseconds =
          convertTimestampSecondsToMiliseconds(executionExpirationTime);

      if (metadataExpirationTimeInMiliseconds > currentExpirationTimeInMiliseconds) {
        continue;
      }

      if (doesExistTypeAndTarget(TaskType.DELETE, metadataWrapper.getId())) {
        continue;
      }

      TaskDescription dataDeletionTaskDescription = new TaskDescription();
      dataDeletionTaskDescription.setJob(DataDeletionExecutor.class.getName());
      dataDeletionTaskDescription.setType(TaskType.DELETE);
      dataDeletionTaskDescription.setStatus(TaskDescription.TaskStatus.PENDING);
      dataDeletionTaskDescription.setTarget(metadataWrapper.getId());

      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.MINUTE, 2);
      Date startTime = calendar.getTime();
      dataDeletionTaskDescription.setStartTime(startTime);

      createNew(dataDeletionTaskDescription);
    }
  }

  private long convertTimestampSecondsToMiliseconds(String timestamp) {
    return Long.parseLong(timestamp) * 1000;
  }
}
