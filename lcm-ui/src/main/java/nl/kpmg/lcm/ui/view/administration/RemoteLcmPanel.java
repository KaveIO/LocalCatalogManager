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
import nl.kpmg.lcm.common.client.ClientException;
import nl.kpmg.lcm.common.data.RemoteLcm;
import nl.kpmg.lcm.common.rest.types.RemoteLcmRepresentation;
import nl.kpmg.lcm.common.rest.types.RemoteLcmsRepresentation;
import nl.kpmg.lcm.ui.component.DefinedLabel;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.components.RemoteLcmCreateWindow;
import nl.kpmg.lcm.ui.view.administration.listeners.DeleteRemoteLcmListener;
import nl.kpmg.lcm.ui.view.administration.listeners.EditRemoteLcmListener;
import nl.kpmg.lcm.ui.view.administration.listeners.ImportUsersFromRemoteLcmListener;
import nl.kpmg.lcm.ui.view.administration.listeners.TestRemoteLcmListener;

import org.slf4j.LoggerFactory;

/**
 *
 * @author mhoekstra
 */

public class RemoteLcmPanel extends CustomComponent implements DynamicDataContainer {
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RemoteLcmPanel.class
      .getName());
  /**
   * The default size of the side panels of this view.
   */
  private static final String PANEL_SIZE = "400px";

  private RemoteLcmsRepresentation remoteLcms;
  private Table remoteLcmTable;
  private Panel detailsPanel;
  private RemoteLcmRepresentation selectedRemoteLcm;

  private RestClientService restClientService;

  public RemoteLcmPanel(RestClientService restClientService) {
    this.restClientService = restClientService;

    VerticalLayout rootVerticalLayout = new VerticalLayout();

    HorizontalLayout menubar = initMenuBar(restClientService);
    rootVerticalLayout.addComponent(menubar);

    HorizontalLayout dataLayout = initDataLayout();
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

    remoteLcmTable = constructTable();
    VerticalLayout tableLayout = new VerticalLayout();
    tableLayout.addComponent(remoteLcmTable);
    tableLayout.addStyleName("padding-right-20");

    detailsPanel = new Panel("Remote LCM details");
    detailsPanel.setWidth(PANEL_SIZE);
    detailsPanel.setHeight("100%");

    HorizontalLayout horizontalLayout = new HorizontalLayout();
    horizontalLayout.addComponent(tableLayout);
    horizontalLayout.addComponent(detailsPanel);
    horizontalLayout.setWidth("100%");
    horizontalLayout.setExpandRatio(tableLayout, 1f);

    return horizontalLayout;
  }

  private HorizontalLayout initMenuBar(RestClientService restClientService1) {
    Button refreshButton = initRefreshButton();
    Button createButton = initCreateButton(restClientService1);

    HorizontalLayout menubar = new HorizontalLayout();
    menubar.setStyleName("v-panel-borderless");
    menubar.addComponent(createButton);
    menubar.addComponent(refreshButton);
    return menubar;
  }

  private Button initRefreshButton() {
    Button refreshButton = new Button("Refresh");
    refreshButton.addClickListener((Button.ClickEvent event) -> {
      refreshRemoteLcmTable();
    });
    refreshButton.addStyleName("margin-10");

    return refreshButton;
  }

  private Button initCreateButton(RestClientService restClientService1) {
    Button createButton = new Button("Create");
    createButton.addStyleName("margin-10");
    createButton.addClickListener((Button.ClickEvent event) -> {
      RemoteLcmCreateWindow remoteLcmCreateWindow =
          new RemoteLcmCreateWindow(restClientService1, this);
      UI.getCurrent().addWindow(remoteLcmCreateWindow);
    });

    return createButton;
  }

  private Table constructTable() throws UnsupportedOperationException {
    Table table = new Table();
    table.setWidth("100%");
    table.addContainerProperty("Name", String.class, null);
    table.addContainerProperty("Protocol", String.class, null);
    table.addContainerProperty("Address", String.class, null);
    table.addContainerProperty("Port", String.class, null);
    table.addContainerProperty("Status", String.class, null);
    table.addContainerProperty("Actions", HorizontalLayout.class, null);

    return table;
  }

  private void reloadRemoteLcms() {
    try {
      this.remoteLcms = restClientService.getRemoteLcm();
    } catch (AuthenticationException | ServerException | ClientException ex) {
      Notification.show("Unable to reload the remoteLcms!");
      LOGGER.error("Unable to reload the remoteLcms." + ex.getMessage());
    }
  }

  private void refreshRemoteLcmTable() {
    reloadRemoteLcms();
    remoteLcmTable.removeAllItems();
    if (remoteLcms != null) {
      for (RemoteLcmRepresentation item : remoteLcms.getItems()) {
        RemoteLcm remoteLcm = item.getItem();

        HorizontalLayout actionsLayout = createActionsLayout(item);
        String code = "";
        if (remoteLcm.getStatus() != null && remoteLcm.getStatus().contains(":")) {
          code = remoteLcm.getStatus().split(":")[0];
        }
        remoteLcmTable.addItem(new Object[] {remoteLcm.getName(), remoteLcm.getProtocol(),
            remoteLcm.getDomain(), remoteLcm.getPort().toString(), code, actionsLayout},
            remoteLcm.getId());
      }
    }
  }

  private void setSelectedRemoteLcm(RemoteLcmRepresentation selectedRemoteLcm) {
    this.selectedRemoteLcm = selectedRemoteLcm;
    updateSelectedRemoteLcm();
  }

  private void updateSelectedRemoteLcm() {
    VerticalLayout panelContent = new VerticalLayout();

    RemoteLcm item = selectedRemoteLcm.getItem();

    panelContent.setMargin(true);
    panelContent.addComponent(new DefinedLabel("Name", item.getName()));
    panelContent.addComponent(new DefinedLabel("Unique LCM id", item.getUniqueId()));
    panelContent.addComponent(new DefinedLabel("Application id", item.getApplicationId()));
    panelContent.addComponent(new DefinedLabel("Protocol", item.getProtocol()));
    panelContent.addComponent(new DefinedLabel("Address", item.getDomain()));
    panelContent.addComponent(new DefinedLabel("Port", item.getPort().toString()));
    String template = "%s://%s:%s";
    String url =
        String.format(template, item.getProtocol(), item.getDomain(), item.getPort().toString());
    panelContent.addComponent(new DefinedLabel("URL", url));
    panelContent.addComponent(new DefinedLabel("Status", item.getStatus()));

    detailsPanel.setContent(panelContent);
  }

  @Override
  public void updateContent() {
    refreshRemoteLcmTable();
  }

  private HorizontalLayout createActionsLayout(RemoteLcmRepresentation item) {

    HorizontalLayout actionsLayout = new HorizontalLayout();

    Button viewButton = new Button("view");
    viewButton.setData(item);
    viewButton.addClickListener((event) -> {
      RemoteLcmRepresentation data = (RemoteLcmRepresentation) event.getButton().getData();
      setSelectedRemoteLcm(data);
    });
    viewButton.addStyleName("link");

    Button editButton = new Button("edit");
    editButton.setData(item);
    editButton.addClickListener(new EditRemoteLcmListener(this, restClientService));
    editButton.addStyleName("link");

    Button deleteButton = new Button("delete");
    deleteButton.setData(item);
    deleteButton.addClickListener(new DeleteRemoteLcmListener(this, restClientService));
    deleteButton.addStyleName("link");

    Button importUsersButton = new Button("import users");
    importUsersButton.setData(item);
    importUsersButton.addClickListener(new ImportUsersFromRemoteLcmListener(this, restClientService));
    importUsersButton.addStyleName("link");

    Button testButton = new Button("test");
    testButton.setData(item);
    testButton.addClickListener(new TestRemoteLcmListener(this, restClientService));
    //testButton.addStyleName("link");

    actionsLayout.addComponent(viewButton);
    actionsLayout.addComponent(editButton);
    actionsLayout.addComponent(deleteButton);
    actionsLayout.addComponent(importUsersButton);
    actionsLayout.addComponent(testButton);

    return actionsLayout;
  }
}
