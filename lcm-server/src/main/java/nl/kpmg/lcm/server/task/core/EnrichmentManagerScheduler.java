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
package nl.kpmg.lcm.server.task.core;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import nl.kpmg.lcm.server.data.TaskSchedule;
import nl.kpmg.lcm.server.data.TaskType;
import nl.kpmg.lcm.server.data.service.TaskScheduleService;
import nl.kpmg.lcm.server.task.TaskScheduleException;
import nl.kpmg.lcm.server.task.enrichment.EnrichmentManagerTask;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author shristov
 */
@Component
public class EnrichmentManagerScheduler {

  @Autowired
  private TaskScheduleService taskScheduleService;


  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentManagerScheduler.class
      .getName());


  /* How often the metadata is scanned for changes in "enrichement-properties section" */
  private static final String DEFAULT_ENRICHMENT_MANAGER_CRON_SCHEDULE = "0 0 * * * ?";

  private static final String GROUP_KEY = "scheduled";

  /**
   * Schedules an EnrichmentTask based on its cron definition.
   *
   * @param taskScheduleItem the execution schedule
   * @throws TaskScheduleException when the task couldn't be scheduled
   */
  public void scheduleEnrichmentManagerTask(Scheduler scheduler,
      TaskSchedule.TaskScheduleItem taskScheduleItem) throws TaskScheduleException {
    if (taskScheduleItem.getTaskType() != TaskType.ENRICHMENT_MANAGER) {
      LOGGER.warn("unable to schedule enrichment manager because task type is wrong. Name: "
          + taskScheduleItem.getName());
      return;
    }

    // Assertions until we have input validation on the database objects
    String name = taskScheduleItem.getName();
    String cron = taskScheduleItem.getCron();
    TaskType taskType = taskScheduleItem.getTaskType();
    if (name == null || cron == null
        || taskScheduleItem.getTaskType() != TaskType.ENRICHMENT_MANAGER) {
      String message =
          String.format(
              "The task with name '%s', and cron '%s' and task type '%s' has missing data!", name,
              cron, taskType);
      throw new TaskScheduleException(message);
    }

    try {
      JobDetail jobDetail =
          newJob(EnrichmentManagerTask.class).withIdentity(name, GROUP_KEY).build();

      CronTrigger trigger =
          newTrigger().withIdentity(name, GROUP_KEY).withSchedule(cronSchedule(cron)).build();

      scheduler.scheduleJob(jobDetail, trigger);
    } catch (SchedulerException ex) {
      LOGGER.error(null, ex);
      throw new TaskScheduleException(ex);
    }
  }

  public TaskSchedule.TaskScheduleItem defaultEnrichmentMangerSchedule() {
    TaskSchedule.TaskScheduleItem taskScheduleItem = new TaskSchedule.TaskScheduleItem();
    taskScheduleItem.setCron(DEFAULT_ENRICHMENT_MANAGER_CRON_SCHEDULE);
    taskScheduleItem.setTarget("*");
    taskScheduleItem.setTaskType(TaskType.ENRICHMENT_MANAGER);
    taskScheduleItem.setJob(EnrichmentManagerTask.class.getName());
    taskScheduleItem.setName("Enrichment manager task");

    return taskScheduleItem;
  }

  public void addDefaultEnrichmentSchedule(Scheduler scheduler) {
    TaskSchedule.TaskScheduleItem defaultEnrichmentManagerSchedule =
        defaultEnrichmentMangerSchedule();
    try {
      scheduleEnrichmentManagerTask(scheduler, defaultEnrichmentManagerSchedule);
      taskScheduleService.createManagerSchedule(defaultEnrichmentManagerSchedule);
    } catch (TaskScheduleException ex) {
      LOGGER.warn("Failed to schedule default enrichment manager", ex);
    }
  }

}