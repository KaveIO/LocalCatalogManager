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

package nl.kpmg.lcm.server.cron.job.processor;

import static org.quartz.JobBuilder.newJob;

import nl.kpmg.lcm.common.data.TaskDescription;
import nl.kpmg.lcm.server.cron.TaskResult;
import nl.kpmg.lcm.server.cron.exception.CronJobExecutionException;
import nl.kpmg.lcm.server.cron.exception.CronJobScheduleException;
import nl.kpmg.lcm.server.cron.job.AbstractDataProcessor;
import nl.kpmg.lcm.server.cron.job.AbstractJobScheduler;
import nl.kpmg.lcm.server.data.service.TaskDescriptionService;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Executor of ad-hoc tasks. AbstractJobScheduler can be created either through a schedule or in an
 * ad-hoc way. These ad-hoc tasks will be scheduled by this Task.
 *
 * @author mhoekstra
 */
public class DataProcessorScheduler extends AbstractJobScheduler {

  private final Logger LOGGER = LoggerFactory.getLogger(DataProcessorScheduler.class.getName());
  /**
   * The group key which is used to register the ad-hoc tasks.
   */
  private static final String GROUP_KEY = "adhoc";

  /**
   * The TaskDescriptionDao.
   */
  @Autowired
  private TaskDescriptionService taskDescriptionService;

  /**
   * Schedules all tasks with the status PENDING for direct execution.
   *
   * @return the result of the scheduling
   * @throws CronJobExecutionException if the task fails to execute
   */
  @Override
  public final TaskResult execute() throws CronJobExecutionException {
    List<TaskDescription> taskDescriptions =
        taskDescriptionService.findByStatus(TaskDescription.TaskStatus.PENDING);
    for (TaskDescription taskDescription : taskDescriptions) {
      try {
        scheduleDataProcessor(taskDescription.getId(), taskDescription.getJob(),
            taskDescription.getTarget());

        taskDescriptionService.updateStatus(taskDescription.getId(),
            TaskDescription.TaskStatus.SCHEDULED);
      } catch (CronJobScheduleException ex) {
        LOGGER.warn("Failed scheduling task.");
        taskDescriptionService.updateStatus(taskDescription.getId(),
            TaskDescription.TaskStatus.FAILED);
      }

    }
    return TaskResult.SUCCESS;
  }

  /**
   * Schedules an AbstractDataProcessor for immediate execution.
   *
   * @param name The name of the job
   * @param job the class name of the job to execute
   * @param target the target MetaData expression
   * @throws CronJobScheduleException when the task couldn't be scheduled
   */
  private void scheduleDataProcessor(final String name, final String job, final String target)
      throws CronJobScheduleException {

    if (name == null || name.isEmpty()) {
      throw new CronJobScheduleException("Name can't be empty for scheduled task");
    }
    if (job == null || job.isEmpty()) {
      throw new CronJobScheduleException("Job can't be empty for scheduled task");
    }
    if (target == null || target.isEmpty()) {
      throw new CronJobScheduleException("Target can't be empty for scheduled task");
    }

    try {
      Class<? extends AbstractDataProcessor> dataProcessorClass = getDataProcessorClass(job);
      scheduleDataProcessor(name, dataProcessorClass, target);
    } catch (CronJobExecutionException ex) {
      LOGGER.error(ex.getMessage());
      throw new CronJobScheduleException(ex);
    }
  }

  /**
   * Schedules an AbstractDataProcessor for immediate execution.
   *
   * @param name The name of the job
   * @param job the class of the job to execute
   * @param target the target MetaData expression
   * @throws CronJobScheduleException when the task couldn't be scheduled
   */
  private void scheduleDataProcessor(final String name,
      final Class<? extends AbstractDataProcessor> job, final String target)
      throws CronJobScheduleException {
    try {
      JobDetail jobDetail = newJob(job).withIdentity(name, GROUP_KEY).build();
      jobDetail.getJobDataMap().put(AbstractDataProcessor.TARGET_KEY, target);
      jobDetail.getJobDataMap().put(AbstractDataProcessor.TASK_ID_KEY, name);

      Scheduler scheduler = getScheduler();
      scheduler.addJob(jobDetail, true, true);
      scheduler.triggerJob(jobDetail.getKey());
    } catch (SchedulerException ex) {
      LOGGER.error(null, ex);
      throw new CronJobScheduleException(ex);
    }
  }

  /**
   * Method for translating a class name to a specific Class instance. This will check if the
   * provided class name actually extends an AbstractDataProcessor
   *
   * @param className the name of the class
   * @return the class instance of the class
   * @throws CronJobExecutionException when className is not correct or points to something else
   *         than an AbstractDataProcessor
   */
  private Class<? extends AbstractDataProcessor> getDataProcessorClass(final String className)
      throws CronJobExecutionException {
    try {
      Class<?> cls = Class.forName(className);
      if (AbstractDataProcessor.class.isAssignableFrom(cls)) {
        return (Class<? extends AbstractDataProcessor>) cls;
      } else {
        throw new CronJobExecutionException("Task definition doesn't contain a schedulable job");
      }
    } catch (ClassNotFoundException ex) {
      LOGGER.error(null, ex);
      throw new CronJobExecutionException(ex);
    }
  }

}