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

package nl.kpmg.lcm.server.task.core;

import static org.quartz.JobBuilder.newJob;

import nl.kpmg.lcm.common.data.TaskDescription;
import nl.kpmg.lcm.server.data.service.TaskDescriptionService;
import nl.kpmg.lcm.server.task.CoreTask;
import nl.kpmg.lcm.server.task.EnrichmentTask;
import nl.kpmg.lcm.server.task.TaskException;
import nl.kpmg.lcm.server.task.TaskResult;
import nl.kpmg.lcm.server.task.TaskScheduleException;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Executor of ad-hoc tasks. EnrichmentTasks can be created either through a schedule or in an
 * ad-hoc way. These ad-hoc tasks will be scheduled by this Task.
 *
 * @author mhoekstra
 */
public class ExecuteTasksCoreTask extends CoreTask {

  private final Logger LOGGER = LoggerFactory.getLogger(ExecuteTasksCoreTask.class.getName());
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
   * @throws TaskException if the task fails to execute
   */
  @Override
  public final TaskResult execute() throws TaskException {

    List<TaskDescription> taskDescriptions =
        taskDescriptionService.findByStatus(TaskDescription.TaskStatus.PENDING);
    for (TaskDescription taskDescription : taskDescriptions) {
      try {
        scheduleEnrichmentTask(taskDescription.getId(), taskDescription.getJob(),
            taskDescription.getTarget());
        taskDescriptionService.updateStatus(taskDescription.getId(),
            TaskDescription.TaskStatus.SCHEDULED);
      } catch (TaskScheduleException ex) {
        LOGGER.warn("Failed scheduling task.");
        taskDescriptionService.updateStatus(taskDescription.getId(),
            TaskDescription.TaskStatus.FAILED);
      }

    }
    return TaskResult.SUCCESS;
  }

  /**
   * Schedules an EnrichmentTask for immediate execution.
   *
   * @param name The name of the job
   * @param job the class name of the job to execute
   * @param target the target MetaData expression
   * @throws TaskScheduleException when the task couldn't be scheduled
   */
  private void scheduleEnrichmentTask(final String name, final String job, final String target)
      throws TaskScheduleException {

    if (name == null || name.isEmpty()) {
      throw new TaskScheduleException("Name can't be empty for scheduled task");
    }
    if (job == null || job.isEmpty()) {
      throw new TaskScheduleException("Job can't be empty for scheduled task");
    }
    if (target == null || target.isEmpty()) {
      throw new TaskScheduleException("Target can't be empty for scheduled task");
    }

    try {
      Class<? extends EnrichmentTask> enrichmentTaskClass = getEnrichmentTaskClass(job);
      scheduleEnrichmentTask(name, enrichmentTaskClass, target);
    } catch (TaskException ex) {
      LOGGER.error(ex.getMessage());
      throw new TaskScheduleException(ex);
    }
  }

  /**
   * Schedules an EnrichmentTask for immediate execution.
   *
   * @param name The name of the job
   * @param job the class of the job to execute
   * @param target the target MetaData expression
   * @throws TaskScheduleException when the task couldn't be scheduled
   */
  private void scheduleEnrichmentTask(final String name, final Class<? extends EnrichmentTask> job,
      final String target) throws TaskScheduleException {
    try {
      JobDetail jobDetail = newJob(job).withIdentity(name, GROUP_KEY).build();
      jobDetail.getJobDataMap().put(EnrichmentTask.TARGET_KEY, target);
      jobDetail.getJobDataMap().put(EnrichmentTask.TASK_ID_KEY, name);

      Scheduler scheduler = getScheduler();
      scheduler.addJob(jobDetail, true, true);
      scheduler.triggerJob(jobDetail.getKey());
    } catch (SchedulerException ex) {
      LOGGER.error( null, ex);
      throw new TaskScheduleException(ex);
    }
  }

  /**
   * Method for translating a class name to a specific Class instance. This will check if the
   * provided class name actually extends an EnrichmentTask
   *
   * @param className the name of the class
   * @return the class instance of the class
   * @throws TaskException when className is not correct or points to something else than an
   *         EnrichmentTask
   */
  private Class<? extends EnrichmentTask> getEnrichmentTaskClass(final String className)
      throws TaskException {
    try {
      Class<?> cls = Class.forName(className);
      if (EnrichmentTask.class.isAssignableFrom(cls)) {
        return (Class<? extends EnrichmentTask>) cls;
      } else {
        throw new TaskException("Task definition doesn't contain a schedulable job");
      }
    } catch (ClassNotFoundException ex) {
      LOGGER.error( null, ex);
      throw new TaskException(ex);
    }
  }
}
