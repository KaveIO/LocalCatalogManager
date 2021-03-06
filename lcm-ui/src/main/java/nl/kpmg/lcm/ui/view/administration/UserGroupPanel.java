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

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.data.UserGroup;
import nl.kpmg.lcm.common.rest.types.UserGroupRepresentation;
import nl.kpmg.lcm.common.rest.types.UserGroupsRepresentation;
import nl.kpmg.lcm.ui.component.DefinedLabel;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.LcmBadRequestException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.components.UserGroupCreateWindow;
import nl.kpmg.lcm.ui.view.administration.listeners.DeleteUserGroupListener;
import nl.kpmg.lcm.ui.view.administration.listeners.EditUserGroupListener;

import org.slf4j.LoggerFactory;

/**
 *
 * @author mhoekstra
 */

public class UserGroupPanel extends CustomComponent implements DynamicDataContainer {
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UserGroupPanel.class
      .getName());
  /**
   * The default size of the side panels of this view.
   */
  private static final String DETAILS_PANEL_WIDTH = "400px";

  private UserGroupsRepresentation userGroups;
  private Table userGroupTable;
  private Panel userGroupDetailsPanel;
  private UserGroupRepresentation selectedUserGroup;

  private final RestClientService restClientService;

  public UserGroupPanel(RestClientService restClientService) {
    this.restClientService = restClientService;

    HorizontalLayout menubar = initMenubar(restClientService);

    HorizontalLayout dataLayout = initDataLayout();

    VerticalLayout rootVerticalLayout = new VerticalLayout();
    rootVerticalLayout.addComponent(menubar);
    rootVerticalLayout.addComponent(dataLayout);
    rootVerticalLayout.setHeight("100%");

    HorizontalLayout root = new HorizontalLayout();
    root.addComponent(rootVerticalLayout);
    root.setSpacing(true);
    root.setMargin(true);
    root.setWidth("100%");
    root.setHeight("100%");
    root.setExpandRatio(rootVerticalLayout, 1f);

    setCompositionRoot(root);
  }

  private HorizontalLayout initDataLayout() throws UnsupportedOperationException {
    VerticalLayout tableLayout = new VerticalLayout();

    userGroupTable = createUserTable();
    tableLayout.addComponent(userGroupTable);
    tableLayout.addStyleName("padding-right-20");

    userGroupDetailsPanel = new Panel("User details");
    userGroupDetailsPanel.setWidth(DETAILS_PANEL_WIDTH);
    userGroupDetailsPanel.setHeight("100%");

    HorizontalLayout dataLayout = new HorizontalLayout();
    dataLayout.addComponent(tableLayout);
    dataLayout.addComponent(userGroupDetailsPanel);
    dataLayout.setWidth("100%");
    dataLayout.setExpandRatio(tableLayout, 1f);

    return dataLayout;
  }

  private HorizontalLayout initMenubar(RestClientService restClientService1) {

    Button createButton = initCreateButton(restClientService1);
    Button refreshButton = initRefreshButton();

    HorizontalLayout menubar = new HorizontalLayout();
    menubar.setStyleName("v-panel-borderless");
    menubar.addComponent(createButton);
    menubar.addComponent(refreshButton);

    return menubar;
  }

  private Button initRefreshButton() {
    Button refreshButton = new Button("Refresh");
    refreshButton.addClickListener((Button.ClickEvent event) -> {
      updateUserTable();
    });
    refreshButton.addStyleName("margin-10");

    return refreshButton;
  }

  private Button initCreateButton(RestClientService restClientService1) {
    Button createButton = new Button("Create");
    createButton.addClickListener((Button.ClickEvent event) -> {
      UserGroupCreateWindow userGroupCreateWindow =
          new UserGroupCreateWindow(restClientService1, this);
      UI.getCurrent().addWindow(userGroupCreateWindow);
    });
    createButton.addStyleName("margin-10");

    return createButton;
  }

  private Table createUserTable() throws UnsupportedOperationException {
    Table table = new Table();
    table.setWidth("100%");
    table.addContainerProperty("Name", String.class, null);
    table.addContainerProperty("Actions", HorizontalLayout.class, null);

    return table;
  }

  private void reloadUsers() {
    try {
      this.userGroups = restClientService.getUserGroups();
    } catch (ServerException ex) {
      Notification.show("Unable to reload the userGroups!");
      LOGGER.error("Unable to reload the userGroups." + ex.getMessage());
    } catch (AuthenticationException | LcmBadRequestException ex) {
      LOGGER.error("Unable to reload the userGroups." + ex.getMessage());
      Notification.show("Unable to reload the userGroups! Message: " + ex.getMessage());
    }
  }

  private void updateUserTable() {
    reloadUsers();
    userGroupTable.removeAllItems();
    if (userGroups != null) {
      for (UserGroupRepresentation item : userGroups.getItems()) {
        UserGroup userGroup = item.getItem();

        HorizontalLayout actionsLayout = createActionsLayout(item);
        userGroupTable
            .addItem(new Object[] {userGroup.getName(), actionsLayout}, userGroup.getId());
      }
    }
  }

  private HorizontalLayout createActionsLayout(UserGroupRepresentation item) {

    HorizontalLayout actionsLayout = new HorizontalLayout();

    Button viewButton = new Button("view");
    viewButton.setData(item);
    viewButton.addClickListener((event) -> {
      UserGroupRepresentation data = (UserGroupRepresentation) event.getButton().getData();
      setSelectedUser(data);
    });
    viewButton.addStyleName("link");

    Button editButton = new Button("edit");
    editButton.setData(item);
    editButton.addClickListener(new EditUserGroupListener(this, restClientService));
    editButton.addStyleName("link");

    Button deleteButton = new Button("delete");
    deleteButton.setData(item);
    deleteButton.addClickListener(new DeleteUserGroupListener(this, restClientService));
    deleteButton.addStyleName("link");

    actionsLayout.addComponent(viewButton);
    actionsLayout.addComponent(editButton);
    actionsLayout.addComponent(deleteButton);

    return actionsLayout;
  }

  private void setSelectedUser(UserGroupRepresentation selectedUser) {
    this.selectedUserGroup = selectedUser;
    refreshDetailsPanel();
  }

  private void refreshDetailsPanel() {
    VerticalLayout panelContent = new VerticalLayout();

    UserGroup item = selectedUserGroup.getItem();
    panelContent.addComponent(new DefinedLabel("Username: ", item.getName()));

    String usersList = "";
    if (item.getUsers() != null && item.getUsers().size() > 0) {
      usersList = item.getUsers().toString();
    }
    panelContent.addComponent(new DefinedLabel("Users: ", usersList));

    String allowedPathList = "";
    if (item.getAllowedPathList() != null && item.getAllowedPathList().size() > 0) {
      allowedPathList = item.getAllowedPathList().toString();
    }
    panelContent.addComponent(new DefinedLabel("Allowed paths: ", allowedPathList));

    String allowedMetadataList = "";
    if (item.getAllowedMetadataList() != null && item.getAllowedMetadataList().size() > 0) {
      allowedMetadataList = item.getAllowedMetadataList().toString();
    }
    panelContent.addComponent(new DefinedLabel("Allowed metadatas: ", allowedMetadataList));

    panelContent.setMargin(true);

    userGroupDetailsPanel.setContent(panelContent);
  }

  @Override
  public void updateContent() {
    updateUserTable();
  }
}