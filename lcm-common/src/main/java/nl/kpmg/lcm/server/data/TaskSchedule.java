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

import java.util.LinkedList;
import java.util.List;
import javax.annotation.security.PermitAll;
import org.springframework.data.mongodb.core.mapping.Document;

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
  private List<TaskScheduleItem> items = new LinkedList();

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
  }

  public List<TaskScheduleItem> getItems() {
    return items;
  }

  public void setItems(List<TaskScheduleItem> items) {
    this.items = items;
  }
}
