/*
 * Copyright 2016 KPMG N.V. (unless otherwise stated).
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

package nl.kpmg.lcm.ui.view.administration;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import nl.kpmg.lcm.rest.types.TaskDescriptionRepresentation;
import nl.kpmg.lcm.rest.types.TaskDescriptionsRepresentation;
import nl.kpmg.lcm.rest.types.TaskScheduleRepresentation;
import nl.kpmg.lcm.server.data.TaskDescription;
import nl.kpmg.lcm.server.data.TaskSchedule;

import java.util.Date;

/**
 *
 * @author mhoekstra
 */
public class TasksPanel extends CustomComponent {

  private TaskScheduleRepresentation taskSchedule;
  private TaskDescriptionsRepresentation tasks;

  private final Table scheduleTable;
  private final Table tasksTable;

  public TasksPanel() {
    Panel panel = new Panel();
    VerticalLayout root = new VerticalLayout();

    Panel schedulePanel = new Panel();
    VerticalLayout schedulePanelLayout = new VerticalLayout();
    scheduleTable = new Table();
    scheduleTable.setWidth("100%");
    scheduleTable.setHeight("300px");
    scheduleTable.addContainerProperty("Name", String.class, null);
    scheduleTable.addContainerProperty("Cron", String.class, null);
    scheduleTable.addContainerProperty("Job", String.class, null);
    scheduleTable.addContainerProperty("Target", String.class, null);
    Label scheduleLabel = new Label("Schedule");
    scheduleLabel.setStyleName("v-label-h2");
    schedulePanelLayout.addComponent(scheduleLabel);
    schedulePanelLayout.addComponent(scheduleTable);
    schedulePanel.setContent(schedulePanelLayout);

    Panel tasksPanel = new Panel();
    VerticalLayout tasksPanelLayout = new VerticalLayout();
    tasksTable = new Table();
    tasksTable.setWidth("100%");
    tasksTable.addContainerProperty("Job", String.class, null);
    tasksTable.addContainerProperty("Target", String.class, null);
    tasksTable.addContainerProperty("Start", Date.class, null);
    tasksTable.addContainerProperty("End", Date.class, null);
    tasksTable.addContainerProperty("Status", String.class, null);
    Label tasksLabel = new Label("Tasks");
    tasksLabel.setStyleName("v-label-h2");
    tasksPanelLayout.addComponent(tasksLabel);
    tasksPanelLayout.addComponent(tasksTable);
    tasksPanel.setContent(tasksPanelLayout);

    root.addComponent(schedulePanel);
    root.addComponent(tasksPanel);

    root.setSpacing(true);
    root.setMargin(true);
    root.setWidth("100%");

    panel.setContent(root);

    setCompositionRoot(panel);
  }

  public void setTaskSchedule(TaskScheduleRepresentation taskSchedule) {
    this.taskSchedule = taskSchedule;
    updateTaskSchedule();
  }

  public void setTasks(TaskDescriptionsRepresentation tasks) {
    this.tasks = tasks;
    updateTasks();
  }

  private void updateTaskSchedule() {
    scheduleTable.removeAllItems();
    if (taskSchedule != null && taskSchedule.getItem() != null) {
      for (TaskSchedule.TaskScheduleItem item : taskSchedule.getItem().getItems()) {
        scheduleTable.addItem(
            new Object[] {item.getName(), item.getCron(), item.getJob(), item.getTarget()},
            item.getName());
      }
    }
  }

  private void updateTasks() {
    tasksTable.removeAllItems();
    if (tasks != null) {
      for (TaskDescriptionRepresentation item : tasks.getItems()) {
        TaskDescription taskDescription = item.getItem();
        tasksTable.addItem(new Object[] {taskDescription.getJob(), taskDescription.getTarget(),
            taskDescription.getStartTime(), taskDescription.getEndTime(),
            taskDescription.getStatus().toString()}, taskDescription.getId());
      }
    }
  }
}
