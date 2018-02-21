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
package nl.kpmg.lcm.ui.view.transfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.data.ProgressIndication;
import nl.kpmg.lcm.common.data.TaskDescription;
import nl.kpmg.lcm.common.data.TaskDescription.TaskStatus;
import nl.kpmg.lcm.common.rest.types.TaskDescriptionRepresentation;
import nl.kpmg.lcm.common.rest.types.TaskDescriptionsRepresentation;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.LcmBadRequestException;
import nl.kpmg.lcm.ui.rest.RestClientService;

import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ListIterator;

/**
 *
 * @author shristov
 */
public class MonitorPanel extends CustomComponent {
  private RestClientService restClientService;

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MonitorPanel.class
      .getName());

  private Table taskTable;
  private Table indicationTable;
  private ComboBox taskStatusComboBox;
  private TextField searchField;
  private VerticalLayout detailsLayout;
  private String ALL_KEY = "All";

  public MonitorPanel(RestClientService restClientService) {
    this.restClientService = restClientService;

    HorizontalLayout searchlayout = initSearchLayout(restClientService);
    HorizontalLayout dataLayout = initDataLayout();

    VerticalLayout rootVerticalLayout = new VerticalLayout();
    rootVerticalLayout.addComponent(searchlayout);
    rootVerticalLayout.addComponent(dataLayout);

    HorizontalLayout root = new HorizontalLayout();
    root.addComponent(rootVerticalLayout);
    root.setSpacing(true);
    root.setMargin(true);
    root.setWidth("100%");
    root.setHeight("100%");
    root.setExpandRatio(rootVerticalLayout, 1f);

    setCompositionRoot(root);

    reloadTasks("", ALL_KEY);
    taskStatusComboBox.select(ALL_KEY);
  }

  private HorizontalLayout initSearchLayout(RestClientService restClientService1)
      throws UnsupportedOperationException {
    taskStatusComboBox = initTaskStatusComboBox();

    searchField = new TextField();
    searchField.addStyleName("margin-right-20");
    searchField.addStyleName("width-search-field");
    searchField.setCaption("Search criteria");
    searchField.setInputPrompt("Type search criteria");

    Button searchButton = new Button("Search");
    searchButton.addClickListener(new SearchListener());

    HorizontalLayout searchlayout = new HorizontalLayout();
    searchlayout.addComponent(taskStatusComboBox);
    searchlayout.addComponent(searchField);
    searchlayout.addComponent(searchButton);
    searchlayout.setComponentAlignment(searchButton, Alignment.BOTTOM_RIGHT);
    searchlayout.addStyleName("margin-bottom-20");
    return searchlayout;
  }

  private ComboBox initTaskStatusComboBox() throws UnsupportedOperationException {
    ComboBox statusComboBox = new ComboBox("Status");

    statusComboBox.addItem(ALL_KEY);
    statusComboBox.setItemCaption(ALL_KEY, ALL_KEY);
    statusComboBox.addItem(TaskStatus.RUNNING);
    statusComboBox.setItemCaption(TaskStatus.RUNNING, TaskStatus.RUNNING.toString());
    statusComboBox.addItem(TaskStatus.FAILED);
    statusComboBox.setItemCaption(TaskStatus.FAILED, TaskStatus.FAILED.toString());
    statusComboBox.addItem(TaskStatus.SUCCESS);
    statusComboBox.setItemCaption(TaskStatus.SUCCESS, TaskStatus.SUCCESS.toString());
    statusComboBox.addItem(TaskStatus.PENDING);
    statusComboBox.setItemCaption(TaskStatus.PENDING, TaskStatus.PENDING.toString());
    statusComboBox.addItem(TaskStatus.SCHEDULED);
    statusComboBox.setItemCaption(TaskStatus.SCHEDULED, TaskStatus.SCHEDULED.toString());

    statusComboBox.addStyleName("margin-right-20");
    statusComboBox.addStyleName("width-search-field");
    statusComboBox.setRequired(true);
    statusComboBox.setInputPrompt("Please select one");

    return statusComboBox;
  }

  private HorizontalLayout initDataLayout() throws UnsupportedOperationException {
    VerticalLayout tableLayout = new VerticalLayout();
    taskTable = initTaskTable();
    tableLayout.addComponent(taskTable);
    tableLayout.addStyleName("padding-right-20");

    detailsLayout = new VerticalLayout();
    detailsLayout.setHeight("100%");
    indicationTable = initIndicationTable();
    detailsLayout.addComponent(indicationTable);

    HorizontalLayout dataLayout = new HorizontalLayout();
    dataLayout.addComponent(tableLayout);
    dataLayout.addComponent(detailsLayout);
    dataLayout.setWidth("100%");

    return dataLayout;
  }

  private Table initIndicationTable() throws UnsupportedOperationException {
    Table table = new Table();
    table.setWidth("100%");
    table.setHeight("100%");
    table.addContainerProperty("Time", String.class, null);
    table.addContainerProperty("Message", String.class, null);

    return table;
  }

  private Table initTaskTable() throws UnsupportedOperationException {
    Table table = new Table();
    table.setWidth("100%");
    table.addContainerProperty("Matadata", String.class, null);
    table.addContainerProperty("Source LCM", String.class, null);
    table.addContainerProperty("Start time", String.class, null);
    table.addContainerProperty("Status", String.class, null);
    table.addContainerProperty("Actions", HorizontalLayout.class, null);

    return table;
  }

  private HorizontalLayout createActionsLayout(TaskDescription item) {

    HorizontalLayout actionsLayout = new HorizontalLayout();

    Button viewButton = new Button("view progress");
    viewButton.setData(item);
    viewButton.addClickListener((event) -> {
      TaskDescription description = (TaskDescription) event.getButton().getData();
      indicationTable.removeAllItems();
      ListIterator li = description.getProgress().listIterator(description.getProgress().size());
      while (li.hasPrevious()) {
        ProgressIndication indication = (ProgressIndication) li.previous();
        indicationTable.addItem(
            new Object[] {indication.getTimestamp().toString(), indication.getMessage()}, null);
      }
    });
    viewButton.addStyleName("link");

    actionsLayout.addComponent(viewButton);

    return actionsLayout;
  }

  class SearchListener implements Button.ClickListener {
    @Override
    public void buttonClick(Button.ClickEvent event) {
      indicationTable.removeAllItems();
      Object statusValue = taskStatusComboBox.getValue();
      if (statusValue == null) {
        Notification.show("Please select a status. It can not be empy!");
        return;
      }

      String searchText = searchField.getValue().toLowerCase();


      reloadTasks(searchText, statusValue);
    }
  }

  private void reloadTasks(String searchText, Object statusValue) {
      TaskStatus status = initStatus(statusValue);
      try {

      TaskDescriptionsRepresentation result = restClientService.getFetchTasks();
      taskTable.removeAllItems();

      if (result != null) {
        for(TaskDescriptionRepresentation item : result.getItems()){
          TaskDescription description = item.getItem();
          String json;
          try {
            json = new ObjectMapper().writeValueAsString(description);
          } catch (JsonProcessingException ex) {
            LOGGER.warn("Unable to convert task description:  " + description.getId()
                + "Exception : " + ex.getMessage());
            continue;
          }

          if (doMeetSearchCriteria(searchText, json)
              && doMeetStatusFilterCriteria(statusValue, status, description.getStatus())) {

            addDescriptionIntoTable(description);
          }
        }
      }
    } catch (ServerException ex) {
      LOGGER.error("Unable to load task descriptions! Message: " + ex.getMessage());
      Notification.show("Unable to load task  descriptions!");
    } catch (AuthenticationException | LcmBadRequestException ex) {
      LOGGER.error("Unable to reload the task descriptions." + ex.getMessage());
      Notification.show("Unable to reload the task descriptions! Message: " + ex.getMessage());
    }
  }

  private boolean doMeetSearchCriteria(String searchText, String jsonBody) {
    return searchText.length() == 0 /* empty criteria */
        || (jsonBody != null && jsonBody.toLowerCase().indexOf(searchText) != -1);
  }

  private boolean doMeetStatusFilterCriteria(Object statusValue, TaskStatus desiredStatus,
      TaskStatus actualStatus) {
    return isSearchingForAll(statusValue) || desiredStatus != null
        && desiredStatus.equals(actualStatus);
  }

  private void addDescriptionIntoTable(TaskDescription description)
      throws UnsupportedOperationException {
    String metadataName = null;
    String detailsRemoteLcm = null;
    if (description.getDetails() != null) {
      metadataName = (String) description.getDetails().get("metadataName");
      detailsRemoteLcm = (String) description.getDetails().get("remoteLcmName");
    }
    String data = metadataName != null ? metadataName : "";
    String remoteLcm = detailsRemoteLcm != null ? detailsRemoteLcm : "";

    Date startDate = description.getStartTime();
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date = startDate != null ? df.format(startDate) : "";
    String status = description.getStatus() != null ? description.getStatus().toString() : "";
    HorizontalLayout actionsLayout = createActionsLayout(description);
    taskTable.addItem(new Object[] {data, remoteLcm, date, status, actionsLayout},
        description.getId());
  }

  private boolean isSearchingForAll(Object statusValue) {
    if (statusValue instanceof String && statusValue.toString().equalsIgnoreCase(ALL_KEY)) {
      return true;
    }
    return false;
  }

  private TaskStatus initStatus(Object statusValue) {
    TaskStatus status = null;
    if (statusValue instanceof TaskStatus) {
      status = (TaskStatus) statusValue;
    }

    return status;
  }
}
