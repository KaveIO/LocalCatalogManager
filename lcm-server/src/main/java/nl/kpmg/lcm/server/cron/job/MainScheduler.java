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

package nl.kpmg.lcm.server.cron.job;


import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import nl.kpmg.lcm.common.data.TaskSchedule;
import nl.kpmg.lcm.common.data.TaskSchedule.TaskScheduleItem;
import nl.kpmg.lcm.common.data.TaskType;
import nl.kpmg.lcm.server.cron.job.manager.TaskManagerScheduler;
import nl.kpmg.lcm.server.cron.exception.CronJobExecutionException;
import nl.kpmg.lcm.server.cron.exception.CronJobScheduleException;
import nl.kpmg.lcm.server.data.service.TaskScheduleService;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import nl.kpmg.lcm.server.cron.TaskResult;

/**
 * TaskSchedule update task. This task will try and install all task items in the most current
 * TaskSchedule. The current implementation doesn't do this with much intelligence. A backoff should
 * be implemented so that a new schedule will only sparsely overwrite the previous schedule.
 *
 * @author mhoekstra
 */
public class MainScheduler extends AbstractJobScheduler {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MainScheduler.class.getName());
  /**
   * The group key which is used to register the scheduled tasks.
   */
  private static final String GROUP_KEY = "scheduled";

  /**
   * The TaskScheduleDao.
   */
  @Autowired
  private TaskScheduleService taskScheduleService;

  @Autowired
  private TaskManagerScheduler enrichmentManagerScheduler;

  /**
   * The currently active TaskSchedule.
   */
  private static TaskSchedule current;

  /**
   * Installs the current TaskSchedule.
   *
   * @return the result of the task
   * @throws CronJobExecutionException if the task fails
   */
  @Override
  public final TaskResult execute() throws CronJobExecutionException {
    TaskSchedule latest = taskScheduleService.findFirstByOrderByIdDesc();
    if (current == null || !current.equals(latest)) {
      try {
        removeTasks(GROUP_KEY);
      } catch (SchedulerException ex) {
        LOGGER.error("couldn't remove the previous schedule.", ex);
        return TaskResult.FAILURE;
      }

      if (latest == null) {
        latest = new TaskSchedule();
      }

      if (latest.getEnrichmentItems() != null) {
        for (TaskScheduleItem taskScheduleItem : latest.getEnrichmentItems()) {
          try {
            scheduleDataProcessor(taskScheduleItem);
          } catch (CronJobScheduleException ex) {
            LOGGER.warn("Failed to schedule ", ex);
          }
        };
      }

      if (latest.getManagerItems() == null || latest.getManagerItems().size() == 0) {
        enrichmentManagerScheduler.addDefaultEnrichmentSchedule(getScheduler());
      } else {
        for (TaskScheduleItem taskScheduleItem : latest.getManagerItems()) {
          try {
            enrichmentManagerScheduler.scheduleTaskManagerExecutor(getScheduler(),
                taskScheduleItem);
          } catch (CronJobScheduleException ex) {
            LOGGER.warn("Failed to schedule ", ex);
          }
        };
      }

      current = latest;
    }
    return TaskResult.SUCCESS;
  }


  /**
   * Schedules an AbstractDataProcessor based on its cron definition.
   *
   * @param TaskScheduleItem taskScheduleItem
   * @throws CronJobScheduleException when the task couldn't be scheduled
   */
  private void scheduleDataProcessor(TaskScheduleItem taskScheduleItem)
      throws CronJobScheduleException {

    // Assertions until we have input validation on the database objects
    if (taskScheduleItem.getJob() == null) {
      throw new CronJobScheduleException(String.format(
          "The job for task with name '%s', target '%s', and cron '%s' is empty",
          taskScheduleItem.getName(), taskScheduleItem.getTarget(), taskScheduleItem.getCron()));
    }

    try {
      Class<?> cls = Class.forName(taskScheduleItem.getJob());
      if (AbstractDataProcessor.class.isAssignableFrom(cls)) {
        scheduleDataProcessor((Class<AbstractDataProcessor>) cls, taskScheduleItem);
      } else {
        throw new CronJobScheduleException("Task definition doesn't contain a schedulable job");
      }
    } catch (ClassNotFoundException ex) {
      LOGGER.error(null, ex);
      throw new CronJobScheduleException(ex);
    }
  }

  /**
   * Schedules an AbstractDataProcessor based on its cron definition.
   *
   * @param job the class of the job to execute
   * @param taskScheduleItem executed item
   * @throws CronJobScheduleException when the task couldn't be scheduled
   */
  private void scheduleDataProcessor(final Class<? extends AbstractDataProcessor> job,
      TaskScheduleItem taskScheduleItem) throws CronJobScheduleException {

    // Assertions until we have input validation on the database objects
    String name = taskScheduleItem.getName();
    String target = taskScheduleItem.getTarget();
    String targetType = taskScheduleItem.getTargetType();
    String cron = taskScheduleItem.getCron();
    TaskType taskType = taskScheduleItem.getTaskType();
    if (name == null || job == null || target == null || cron == null || taskType == null) {
      throw new CronJobScheduleException(String.format(
          "The job %s for task with name '%s', target '%s', and cron '%s' and task type %s"
              + " can't be scheduled due to missing values", job, name, target, cron, taskType));
    }

    try {
      JobDetail jobDetail = newJob(job).withIdentity(name, GROUP_KEY).build();

      jobDetail.getJobDataMap().put(AbstractDataProcessor.TARGET_KEY, target);
      jobDetail.getJobDataMap().put(AbstractDataProcessor.TARGET_TYPE_KEY, targetType);
      jobDetail.getJobDataMap().put(AbstractDataProcessor.TASK_TYPE_KEY, taskType);

      CronTrigger trigger =
          newTrigger().withIdentity(name, GROUP_KEY).withSchedule(cronSchedule(cron)).build();

      Scheduler scheduler = getScheduler();
      scheduler.scheduleJob(jobDetail, trigger);
    } catch (SchedulerException ex) {
      LOGGER.error(null, ex);
      throw new CronJobScheduleException(ex);
    }
  }

  /**
   * Remove all the scheduled task.
   *
   * @throws SchedulerException if a job can't be removed
   */
  private void removeTasks(String groupKey) throws SchedulerException {
    Scheduler scheduler = getScheduler();
    Set<JobKey> jobKeys =
        scheduler.getJobKeys((GroupMatcher<JobKey>) GroupMatcher.jobGroupEquals(groupKey));

    jobKeys.stream().forEach((jobKey) -> {
      try {
        scheduler.deleteJob(jobKey);
      } catch (SchedulerException ex) {
        LOGGER.error("failed removing task", ex);
      }
    });
  }
}