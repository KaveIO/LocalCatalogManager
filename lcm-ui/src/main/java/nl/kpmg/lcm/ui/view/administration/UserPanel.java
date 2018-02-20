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
import com.vaadin.ui.Window;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.client.ClientException;
import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.rest.types.UserRepresentation;
import nl.kpmg.lcm.common.rest.types.UsersRepresentation;
import nl.kpmg.lcm.ui.component.DefinedLabel;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.components.UserCreateWindow;
import nl.kpmg.lcm.ui.view.administration.listeners.DeleteUserListener;
import nl.kpmg.lcm.ui.view.administration.listeners.EditUserListener;

import org.slf4j.LoggerFactory;

import java.util.List;

import javax.ws.rs.core.Link;

/**
 *
 * @author mhoekstra
 */
public class UserPanel extends CustomComponent implements DynamicDataContainer {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UserPanel.class.getName());
  /**
   * The default size of the side panels of this view.
   */
  private static final String DETAILS_PANEL_WIDTH = "400px";

  private UsersRepresentation users;
  private Table userTable;
  private Panel userDetailsPanel;
  private UserRepresentation selectedUser;

  private final RestClientService restClientService;

  public UserPanel(RestClientService restClientService) {
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
    updateUserTable();
  }

  private HorizontalLayout initDataLayout() throws UnsupportedOperationException {
    VerticalLayout tableLayout = new VerticalLayout();

    userTable = createUserTable();
    tableLayout.addComponent(userTable);
    tableLayout.addStyleName("padding-right-20");

    userDetailsPanel = new Panel("User details");
    userDetailsPanel.setWidth(DETAILS_PANEL_WIDTH);
    userDetailsPanel.setHeight("100%");

    HorizontalLayout dataLayout = new HorizontalLayout();
    dataLayout.addComponent(tableLayout);
    dataLayout.addComponent(userDetailsPanel);
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
      UserCreateWindow userCreateWindow = new UserCreateWindow(restClientService1);
      userCreateWindow.addCloseListener(new Window.CloseListener() {
        @Override
        public void windowClose(Window.CloseEvent e) {
          updateUserTable();
        }
      });
      UI.getCurrent().addWindow(userCreateWindow);
    });
    createButton.addStyleName("margin-10");

    return createButton;
  }

  private Table createUserTable() throws UnsupportedOperationException {
    Table table = new Table();
    table.setWidth("100%");
    table.addContainerProperty("Name", String.class, null);
    table.addContainerProperty("Role", String.class, null);
    table.addContainerProperty("Origin", String.class, null);
    table.addContainerProperty("Actions", HorizontalLayout.class, null);

    return table;
  }

  private void reloadUsers() {
    try {
      this.users = restClientService.getUsers();
    } catch (AuthenticationException | ServerException | ClientException ex) {
      Notification.show("Unable to reload the users!");
      LOGGER.error("Unable to reload the users." + ex.getMessage());
    }
  }

  private void updateUserTable() {
    reloadUsers();
    userTable.removeAllItems();
    if (users != null) {
      for (UserRepresentation item : users.getItems()) {
        User user = item.getItem();

        HorizontalLayout actionsLayout = createActionsLayout(item);
        userTable.addItem(new Object[] {user.getName(), user.getRole(), user.getOrigin(),
            actionsLayout}, user.getId());
      }
    }
  }

  private HorizontalLayout createActionsLayout(UserRepresentation item) {

    HorizontalLayout actionsLayout = new HorizontalLayout();

    Button viewButton = new Button("view");
    viewButton.setData(item);
    viewButton.addClickListener((event) -> {
      UserRepresentation data = (UserRepresentation) event.getButton().getData();
      setSelectedUser(data);
    });
    viewButton.addStyleName("link");

    Button editButton = new Button("edit");
    editButton.setData(item);
    editButton.addClickListener(new EditUserListener(this, restClientService));
    editButton.addStyleName("link");

    Button deleteButton = new Button("delete");
    deleteButton.setData(item);
    deleteButton.addClickListener(new DeleteUserListener(this, restClientService));
    deleteButton.addStyleName("link");

    actionsLayout.addComponent(viewButton);
    actionsLayout.addComponent(editButton);
    actionsLayout.addComponent(deleteButton);

    return actionsLayout;
  }

  private void setSelectedUser(UserRepresentation selectedUser) {
    this.selectedUser = selectedUser;
    refreshDetailsPanel();
  }

  private void refreshDetailsPanel() {
    VerticalLayout panelContent = new VerticalLayout();

    User item = selectedUser.getItem();
    List<Link> links = selectedUser.getLinks();

    panelContent.addComponent(new DefinedLabel("Id: ", item.getId()));

    panelContent.addComponent(new DefinedLabel("Username: ", item.getName()));

    panelContent.addComponent(new DefinedLabel("Role: ", item.getRole()));

    panelContent.addComponent(new DefinedLabel("Origin: ", item.getOrigin()));

    String allowedPathList = "";
    if (item.getAllowedPathList() != null) {
      allowedPathList = item.getAllowedPathList().toString();
    }
    panelContent.addComponent(new DefinedLabel("Allowed paths: ", allowedPathList));

    String allowedMetadataList = "";
    if (item.getAllowedMetadataList() != null) {
      allowedMetadataList = item.getAllowedMetadataList().toString();
    }
    panelContent.addComponent(new DefinedLabel("Allowed metadatas: ", allowedMetadataList));

    panelContent.setMargin(true);

    userDetailsPanel.setContent(panelContent);
  }

  @Override
  public void updateContent() {
    updateUserTable();
  }
}
