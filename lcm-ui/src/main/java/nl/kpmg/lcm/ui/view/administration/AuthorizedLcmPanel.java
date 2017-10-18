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
import nl.kpmg.lcm.common.data.AuthorizedLcm;
import nl.kpmg.lcm.common.rest.types.AuthorizedLcmRepresentation;
import nl.kpmg.lcm.common.rest.types.AuthorizedLcmsRepresentation;
import nl.kpmg.lcm.ui.component.DefinedLabel;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.components.AuthorizedLcmCreateWindow;
import nl.kpmg.lcm.ui.view.administration.listeners.DeleteAuthorizedLcmListener;
import nl.kpmg.lcm.ui.view.administration.listeners.EditAuthorizedLcmListener;

import org.slf4j.LoggerFactory;

/**
 *
 * @author mhoekstra
 */

public class AuthorizedLcmPanel extends CustomComponent implements DynamicDataContainer {
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AuthorizedLcmPanel.class
      .getName());
  /**
   * The default size of the side panels of this view.
   */
  private static final String DETAILS_PANEL_WIDTH = "400px";

  private AuthorizedLcmsRepresentation authorizedLcms;
  private Table authorizedLcmsTable;
  private Panel authorizedLcmPanel;
  private AuthorizedLcmRepresentation selectedAuthorizedLcm;

  private final RestClientService restClientService;

  public AuthorizedLcmPanel(RestClientService restClientService) {
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
    updateAuthorizedLcmTable();
  }

  private HorizontalLayout initDataLayout() throws UnsupportedOperationException {
    VerticalLayout tableLayout = new VerticalLayout();

    authorizedLcmsTable = createAuthorizedLcmTable();
    tableLayout.addComponent(authorizedLcmsTable);
    tableLayout.addStyleName("padding-right-20");

    authorizedLcmPanel = new Panel("Authorized LCM details");
    authorizedLcmPanel.setWidth(DETAILS_PANEL_WIDTH);
    authorizedLcmPanel.setHeight("100%");

    HorizontalLayout dataLayout = new HorizontalLayout();
    dataLayout.addComponent(tableLayout);
    dataLayout.addComponent(authorizedLcmPanel);
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
      updateAuthorizedLcmTable();
    });
    refreshButton.addStyleName("margin-10");

    return refreshButton;
  }

  private Button initCreateButton(RestClientService restClientService1) {
    Button createButton = new Button("Create");
    createButton.addClickListener((Button.ClickEvent event) -> {
      AuthorizedLcmCreateWindow authorizedLcmCreateWindow = new AuthorizedLcmCreateWindow(restClientService1);
      authorizedLcmCreateWindow.addCloseListener(new Window.CloseListener() {
        @Override
        public void windowClose(Window.CloseEvent e) {
          updateAuthorizedLcmTable();
        }
      });
      UI.getCurrent().addWindow(authorizedLcmCreateWindow);
    });
    createButton.addStyleName("margin-10");

    return createButton;
  }

  private Table createAuthorizedLcmTable() throws UnsupportedOperationException {
    Table table = new Table();
    table.setWidth("100%");
    table.addContainerProperty("Name", String.class, null);
    table.addContainerProperty("Unique LCM id", String.class, null);
    table.addContainerProperty("Application id", String.class, null);
    table.addContainerProperty("Actions", HorizontalLayout.class, null);

    return table;
  }

  private void reloadAuthorizedLcms() {
    try {
      this.authorizedLcms = restClientService.getAuthorizedLcms();
    } catch (AuthenticationException | ServerException | ClientException ex) {
      Notification.show("Unable to reload the authorized LCM!");
      LOGGER.error("Unable to reload the authorized LCM." + ex.getMessage());
    }
  }

  private void updateAuthorizedLcmTable() {
    reloadAuthorizedLcms();
    authorizedLcmsTable.removeAllItems();
    if (authorizedLcms != null) {
      for (AuthorizedLcmRepresentation item : authorizedLcms.getItems()) {
        AuthorizedLcm lcm = item.getItem();

        HorizontalLayout actionsLayout = createActionsLayout(item);

        authorizedLcmsTable.addItem(new Object[] {lcm.getName(), lcm.getUniqueId(), lcm.getApplicationId(),
            actionsLayout}, lcm.getId());
      }
    }
  }

  private HorizontalLayout createActionsLayout(AuthorizedLcmRepresentation item) {

    HorizontalLayout actionsLayout = new HorizontalLayout();

    Button viewButton = new Button("view");
    viewButton.setData(item);
    viewButton.addClickListener((event) -> {
      AuthorizedLcmRepresentation data = (AuthorizedLcmRepresentation) event.getButton().getData();
      setSelectedAuthorizedLcm(data);
    });
    viewButton.addStyleName("link");

    Button editButton = new Button("edit");
    editButton.setData(item);
    editButton.addClickListener(new EditAuthorizedLcmListener(this, restClientService));
    editButton.addStyleName("link");

    Button deleteButton = new Button("delete");
    deleteButton.setData(item);
    deleteButton.addClickListener(new DeleteAuthorizedLcmListener(this, restClientService));
    deleteButton.addStyleName("link");


    actionsLayout.addComponent(viewButton);
    actionsLayout.addComponent(editButton);
    actionsLayout.addComponent(deleteButton);

    return actionsLayout;
  }

  private void setSelectedAuthorizedLcm(AuthorizedLcmRepresentation selectedAuthorizedLcm) {
    this.selectedAuthorizedLcm = selectedAuthorizedLcm;
    refreshDetailsPanel();
  }

  private void refreshDetailsPanel() {
    VerticalLayout panelContent = new VerticalLayout();

    AuthorizedLcm item = selectedAuthorizedLcm.getItem();

    panelContent.setMargin(true);

    panelContent.addComponent(new DefinedLabel("Name: ", item.getName()));
    panelContent.addComponent(new DefinedLabel("Lcm Id: ", item.getUniqueId()));
    panelContent.addComponent(new DefinedLabel("Application Id: ", item.getApplicationId()));

    authorizedLcmPanel.setContent(panelContent);
  }

  @Override
  public void updateContent() {
    updateAuthorizedLcmTable();
  }
}
