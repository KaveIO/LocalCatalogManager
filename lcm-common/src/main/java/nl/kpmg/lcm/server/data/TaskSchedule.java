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

package nl.kpmg.lcm.server.data;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.annotation.security.PermitAll;

/**
 * Contains the schedule of tasks.
 *
 * @author mhoekstra
 */
@Document(collection = "taskschedule")
@PermitAll
public class TaskSchedule extends AbstractModel {

  /**
   * The schedule. s
   */
  private List<TaskScheduleItem> enrichmentItems = new LinkedList();
  private List<TaskScheduleItem> fetchItems = new LinkedList();
  private List<TaskScheduleItem> managerItems = new LinkedList();

  /**
   * An inner class describing a single schedule item.
   */
  public static class TaskScheduleItem {
    /**
     * The name of the task.
     */
    private String name;

    /**
     * The cron definition on when this task should run.
     */
    private String cron;

    /**
     * The name of the class which contains the executable code.
     */
    private String job;

    /**
     * The target expression describing on what the task should run.
     */
    private String target;

    /**
     * The target expression describing target type i.e. metadata or storage. The value should be
     * the representing class name.
     */
    private String targetType;

    private TaskType taskType;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getCron() {
      return cron;
    }

    public void setCron(String cron) {
      this.cron = cron;
    }

    public String getJob() {
      return job;
    }

    public void setJob(String job) {
      this.job = job;
    }

    public String getTarget() {
      return target;
    }

    public void setTarget(String target) {
      this.target = target;
    }

    /**
     * @return the targetType
     */
    public String getTargetType() {
      return targetType;
    }

    /**
     * @param targetType the targetType to set
     */
    public void setTargetType(String targetType) {
      this.targetType = targetType;
    }

    @Override
    public boolean equals(Object compared) {
      if (compared == null || !(compared instanceof TaskScheduleItem)) {
        return false;
      }

      if (compared == this) {
        return true;
      }

      TaskScheduleItem comparedItem = (TaskScheduleItem) compared;
      if ((name == null && comparedItem.getName() == null || name.equals(comparedItem.getName()))
          && (cron == null && comparedItem.getCron() == null || cron.equals(comparedItem.getCron()))
          && (target == null && comparedItem.getTarget() == null || target.equals(comparedItem
              .getTarget()))
          && (targetType == null && comparedItem.getTargetType() == null || targetType
              .equals(comparedItem.getTargetType()))
          && (job == null && comparedItem.getJob() == null || job.equals(comparedItem.getJob()))
          && taskType.equals(comparedItem.getTaskType())) {

        return true;
      }

      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, cron, target, targetType, job, taskType);
    }

    /**
     * @return the taskType
     */
    public TaskType getTaskType() {
      return taskType;
    }

    /**
     * @param taskType the taskType to set
     */
    public void setTaskType(TaskType taskType) {
      this.taskType = taskType;
    }

  }

  @Override
  public boolean equals(Object compared) {
    if (compared == null)
      return false;
    if (compared == this)
      return true;
    if (!(compared instanceof TaskSchedule))
      return false;

    TaskSchedule comparedTaskSchedule = (TaskSchedule) compared;

    if (enrichmentItems.equals(comparedTaskSchedule.getEnrichmentItems())
        && fetchItems.equals(comparedTaskSchedule.getFetchItems())
        && managerItems.equals(comparedTaskSchedule.getManagerItems())) {
      return true;
    }


    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(enrichmentItems, fetchItems, managerItems);
  }

  /**
   * @return the enrichmentItems
   */
  public List<TaskScheduleItem> getEnrichmentItems() {
    return enrichmentItems;
  }

  /**
   * @param enrichmentItems the enrichmentItems to set
   */
  public void setEnrichmentItems(List<TaskScheduleItem> enrichmentItems) {
    this.enrichmentItems = enrichmentItems;
  }

  /**
   * @return the fetchItems
   */
  public List<TaskScheduleItem> getFetchItems() {
    return fetchItems;
  }

  /**
   * @param fetchItems the fetchItems to set
   */
  public void setFetchItems(List<TaskScheduleItem> fetchItems) {
    this.fetchItems = fetchItems;
  }

  /**
   * @return the managerItems
   */
  public List<TaskScheduleItem> getManagerItems() {
    return managerItems;
  }

  /**
   * @param managerItems the managerItems to set
   */
  public void setManagerItems(List<TaskScheduleItem> managerItems) {
    this.managerItems = managerItems;
  }

}