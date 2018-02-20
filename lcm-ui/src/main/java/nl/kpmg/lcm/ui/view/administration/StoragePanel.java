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
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.rest.types.StorageRepresentation;
import nl.kpmg.lcm.common.rest.types.StoragesRepresentation;
import nl.kpmg.lcm.ui.component.DefinedLabel;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.components.StorageCreateWindow;
import nl.kpmg.lcm.ui.view.administration.listeners.DeleteStorageListener;
import nl.kpmg.lcm.ui.view.administration.listeners.EditStorageListener;
import nl.kpmg.lcm.ui.view.administration.listeners.TestStorageListener;

import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Link;

/**
 *
 * @author mhoekstra
 */

public class StoragePanel extends CustomComponent implements DynamicDataContainer {
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StoragePanel.class
      .getName());
  /**
   * The default size of the side panels of this view.
   */
  private static final String DETAILS_PANEL_WIDTH = "400px";

  private StoragesRepresentation storages;
  private Table storageTable;
  private Panel storageDetailsPanel;
  private StorageRepresentation selectedStorage;

  private final RestClientService restClientService;

  public StoragePanel(RestClientService restClientService) {
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

    storageTable = createStorageTable();
    tableLayout.addComponent(storageTable);
    tableLayout.addStyleName("padding-right-20");

    storageDetailsPanel = new Panel("Storage details");
    storageDetailsPanel.setWidth(DETAILS_PANEL_WIDTH);
    storageDetailsPanel.setHeight("100%");

    HorizontalLayout dataLayout = new HorizontalLayout();
    dataLayout.addComponent(tableLayout);
    dataLayout.addComponent(storageDetailsPanel);
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
      updateStorageTable();
    });
    refreshButton.addStyleName("margin-10");

    return refreshButton;
  }

  private Button initCreateButton(RestClientService restClientService1) {
    Button createButton = new Button("Create");
    createButton.addClickListener((Button.ClickEvent event) -> {
      StorageCreateWindow storageCreateWindow = new StorageCreateWindow(restClientService1, this);
      UI.getCurrent().addWindow(storageCreateWindow);
    });
    createButton.addStyleName("margin-10");

    return createButton;
  }

  private Table createStorageTable() throws UnsupportedOperationException {
    Table table = new Table();
    table.setWidth("100%");
    table.addContainerProperty("Name", String.class, null);
    table.addContainerProperty("Type", String.class, null);
    table.addContainerProperty("Status", String.class, null);
    table.addContainerProperty("Actions", HorizontalLayout.class, null);

    return table;
  }

  private void reloadStorages() {
    try {
      this.storages = restClientService.getStorage();
    } catch (AuthenticationException | ServerException | ClientException ex) {
      Notification.show("Unable to reload the storages!");
      LOGGER.error("Unable to reload the storages." + ex.getMessage());
    }
  }

  private void updateStorageTable() {
    reloadStorages();
    storageTable.removeAllItems();
    if (storages != null) {
      for (StorageRepresentation item : storages.getItems()) {
        Storage storage = item.getItem();

        HorizontalLayout actionsLayout = createActionsLayout(item);
        String code = "";
        if (storage.getStatus() != null && storage.getStatus().contains(":")) {
          code = storage.getStatus().split(":")[0];
        }
        storageTable.addItem(new Object[] {storage.getName(), storage.getType(), code,
            actionsLayout}, storage.getId());
      }
    }
  }

  private HorizontalLayout createActionsLayout(StorageRepresentation item) {

    HorizontalLayout actionsLayout = new HorizontalLayout();

    Button viewButton = new Button("view");
    viewButton.setData(item);
    viewButton.addClickListener((event) -> {
      StorageRepresentation data = (StorageRepresentation) event.getButton().getData();
      setSelectedStorage(data);
    });
    viewButton.addStyleName("link");

    Button editButton = new Button("edit");
    editButton.setData(item);
    editButton.addClickListener(new EditStorageListener(this, restClientService));
    editButton.addStyleName("link");

    Button deleteButton = new Button("delete");
    deleteButton.setData(item);
    deleteButton.addClickListener(new DeleteStorageListener(this, restClientService));
    deleteButton.addStyleName("link");

    Button testButton = new Button("test connection");
    testButton.setData(item);
    testButton.addClickListener(new TestStorageListener(this, restClientService));
    testButton.addStyleName("link");

    actionsLayout.addComponent(viewButton);
    actionsLayout.addComponent(editButton);
    actionsLayout.addComponent(deleteButton);
    actionsLayout.addComponent(testButton);

    return actionsLayout;
  }

  private void setSelectedStorage(StorageRepresentation selectedStorage) {
    this.selectedStorage = selectedStorage;
    refreshDetailsPanel();
  }

  private void refreshDetailsPanel() {
    VerticalLayout panelContent = new VerticalLayout();

    Storage item = selectedStorage.getItem();
    List<Link> links = selectedStorage.getLinks();

    panelContent.setMargin(true);

    Map<String, String> options = item.getOptions();

    for (Map.Entry<String, String> entry : options.entrySet()) {
      panelContent.addComponent(new DefinedLabel(entry.getKey(), entry.getValue()));
    }

    Map<String, Object> enrichentProperties = item.getEnrichmentProperties();

    if (enrichentProperties != null) {
      for (Map.Entry<String, Object> entry : enrichentProperties.entrySet()) {
        panelContent.addComponent(new DefinedLabel("Enrichment: " + entry.getKey(), entry
            .getValue().toString()));
      }
    }

    panelContent.addComponent(new DefinedLabel("Status: ", item.getStatus()));

    storageDetailsPanel.setContent(panelContent);
  }

  @Override
  public void updateContent() {
    updateStorageTable();
  }
}
