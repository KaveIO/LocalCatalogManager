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

import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.TaskDescription;
import nl.kpmg.lcm.common.data.TaskType;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.cron.TaskResult;
import nl.kpmg.lcm.server.cron.exception.CronJobExecutionException;
import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.data.service.StorageService;
import nl.kpmg.lcm.server.data.service.TaskDescriptionService;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A task which is being applied on MetaData to enrich its content.
 *
 * @author mhoekstra
 */
public abstract class AbstractDataProcessor implements Job {
  protected String taskId;

  private final Logger LOGGER = LoggerFactory.getLogger(AbstractDataProcessor.class.getName());

  /**
   * The id of the object that will be processed. If metadata is transfered the this is metadata id.
   */
  public static final String TARGET_KEY = "target";
  /**
   * Point the type of the processed object i.e. storage, metadata etc.
   */
  public static final String TARGET_TYPE_KEY = "target-type";

  /**
   * The field name containing the task id in the JobDataMap.
   */
  public static final String TASK_ID_KEY = "task-id";

    /**
   * The field name containing the task type in the JobDataMap.
   */
  public static final String TASK_TYPE_KEY = "task-type";

  /**
   * The MetaDataService.
   */
  @Autowired
  protected MetaDataService metaDataService;

  @Autowired
  protected StorageService storageService;

  /**
   * The TaskDescriptionService.
   */
  @Autowired
  protected TaskDescriptionService taskDescriptionService;

  /**
   * Method called to process the actual code of this task.
   *
   * @param metadata the MetaData to apply this task on
   * @param options - any option that could be useful during task execution
   * @return The result of the task
   * @throws CronJobExecutionException if the task can't be executed properly
   */
  protected abstract TaskResult execute(MetaDataWrapper metadataWrapper, Map options)
      throws CronJobExecutionException;

  /**
   * Execute method invoked by the quartz scheduler.
   *
   * Interprets the target in the JobDataMap and invokes execute for each of them. If the task fails
   * for any of the MetaData the execution cycle will keep going.
   *
   * @param context provided by quartz
   * @throws JobExecutionException if the job couldn't be executed properly.
   */
  @Override
  public final void execute(final JobExecutionContext context) throws JobExecutionException {

    // Fetch information from the job context.
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    String target = jobDataMap.getString(TARGET_KEY);
    taskId = jobDataMap.getString(TASK_ID_KEY);
    String targetType = jobDataMap.getString(TARGET_TYPE_KEY);
    TaskType taskType = (TaskType)jobDataMap.get(TASK_TYPE_KEY);
    if (targetType == null) {
      // Metadata is the default target.
      targetType = MetaData.class.getName();
    }
    // Update or create the task description
    if (taskId == null) {
      TaskDescription newTaskDescription = new TaskDescription();
      newTaskDescription.setJob(this.getClass().getName());
      newTaskDescription.setTarget(target);
      newTaskDescription.setType(taskType);
      taskId = taskDescriptionService.createNew(newTaskDescription).getId();
    }

    TaskDescription taskDescription = taskDescriptionService.findOne(taskId);
    if(taskDescription == null) {
        LOGGER.error("Trying to execute not existing task. Id: " + taskId);
        return;
    }

    TaskDescription.TaskStatus status =  TaskDescription.TaskStatus.FAILED;
    try {
        taskDescriptionService.markTaskAsRunning(taskDescription);

        // Find the MetaData target on which this job needs to be executed
        List<MetaData> targets = loadTargetMetadata(target, targetType, taskDescription);

        status = processTargets(targets, taskDescription);
    } finally {
        taskDescriptionService.markTaskAsFinished(taskId, status);
    }
  }

  private TaskDescription.TaskStatus processTargets(List<MetaData> targets,
      TaskDescription taskDescription) {
    // Execute the actuall code for each target
    TaskDescription.TaskStatus status = TaskDescription.TaskStatus.SUCCESS;
    if (targets != null) {
      for (MetaData metadata : targets) {
        try {
          LOGGER.info(String.format("Executing Task %s (%s)", taskDescription.getId(),
              taskDescription.getJob()));

          TaskResult taskResult =
              execute(new MetaDataWrapper(metadata), taskDescription.getOptions());

          LOGGER.info(String.format("Done with Task %s (%s) with status : %s",
              taskDescription.getId(), taskDescription.getJob(), taskResult));
          if (taskResult == TaskResult.FAILURE) {
            status = TaskDescription.TaskStatus.FAILED;
          }
        } catch (CronJobExecutionException ex) {
          LOGGER.error("Failed executing task! Message : " +  ex.getMessage());
        }
      }
    }
    return status;
  }

  private List<MetaData> loadTargetMetadata(String target, String targetType,
      TaskDescription taskDescription) {
    List<MetaData> targets;
    if (target.equals("*")) {
      targets = metaDataService.findAll();
    } else {
      if (targetType.equals(Storage.class.getName())) {
        Storage storage = storageService.findById(target);
        targets = metaDataService.findByStorageName(storage.getName());
        taskDescription.setOptions(storage.getEnrichmentProperties());
      } else if (targetType.equals(MetaData.class.getName())) {
        MetaData metadata = metaDataService.findById(target);
        targets = new LinkedList();
        if (metadata != null) {
          targets.add(metadata);
        }
      } else {
        LOGGER.error("Unable to find target type.");
        taskDescriptionService.markTaskAsFinished(taskId, TaskDescription.TaskStatus.FAILED);
        targets = null;
      }
    }
    return targets;
  }
}