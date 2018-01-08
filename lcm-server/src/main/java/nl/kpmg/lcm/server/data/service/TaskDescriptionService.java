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
import nl.kpmg.lcm.server.data.dao.TaskDescriptionDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

  public List<TaskDescription> findAll() {
    return Lists.newLinkedList(taskDescriptionDao.findAll());
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
}
