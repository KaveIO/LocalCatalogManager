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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import nl.kpmg.lcm.client.ClientException;
import nl.kpmg.lcm.rest.types.StorageRepresentation;
import nl.kpmg.lcm.rest.types.StoragesRepresentation;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.ui.component.DefinedLabel;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.components.StorageCreateWindow;

import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Link;

/**
 *
 * @author mhoekstra
 */

public class StoragePanel extends CustomComponent {
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StoragePanel.class
      .getName());
  /**
   * The default size of the side panels of this view.
   */
  private static final String DETAILS_PANEL_WIDTH = "400px";

  private StoragesRepresentation storages;
  private final Table storageTable;
  private final Panel storageDetailsPanel = new Panel("Storage details");
  private StorageRepresentation selectedStorage;

  private final RestClientService restClientService;

  public StoragePanel(RestClientService restClientService) {
    this.restClientService = restClientService;

    HorizontalLayout menubar = new HorizontalLayout();
    menubar.setStyleName("v-panel-borderless");

    Button createButton = initCreateButton(restClientService);
    Button refreshButton = initRefreshButton();
    menubar.addComponent(createButton);
    menubar.addComponent(refreshButton);

    VerticalLayout storageTablePanelLayout = new VerticalLayout();
    storageTable = createStorageTable();
    storageTablePanelLayout.addComponent(storageTable);
    storageTablePanelLayout.addStyleName("padding-right-20");

    HorizontalLayout storageHorizontalLayout = new HorizontalLayout();
    storageHorizontalLayout.addComponent(storageTablePanelLayout);
    storageHorizontalLayout.addComponent(storageDetailsPanel);
    storageHorizontalLayout.setWidth("100%");
    storageHorizontalLayout.setExpandRatio(storageTablePanelLayout, 1f);

    VerticalLayout storagePanelLayout = new VerticalLayout();
    storagePanelLayout.addComponent(menubar);
    storagePanelLayout.addComponent(storageHorizontalLayout);
    storagePanelLayout.setHeight("100%");

    storageDetailsPanel.setWidth(DETAILS_PANEL_WIDTH);
    storageDetailsPanel.setHeight("100%");

    HorizontalLayout root = new HorizontalLayout();
    root.addComponent(storagePanelLayout);
    root.setSpacing(true);
    root.setMargin(true);
    root.setWidth("100%");
    root.setHeight("100%");
    root.setExpandRatio(storagePanelLayout, 1f);

    setCompositionRoot(root);
    updateStorageTable();
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
      StorageCreateWindow storageCreateWindow = new StorageCreateWindow(restClientService1);
      storageCreateWindow.addCloseListener(new Window.CloseListener() {
        @Override
        public void windowClose(Window.CloseEvent e) {
          updateStorageTable();
        }
      });
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

        storageTable.addItem(new Object[] {storage.getName(), storage.getType(),
            actionsLayout}, storage.getId());
      }
    }
  }

  private HorizontalLayout createActionsLayout(StorageRepresentation item) {

    HorizontalLayout actionsLayout = new HorizontalLayout();

    Button viewButton = new Button("view");
    viewButton.setData(item);
    viewButton.addClickListener(new ViewStorageListenerImpl(this));
    viewButton.addStyleName("link");

    Button editButton = new Button("edit");
    editButton.setData(item);
    editButton.addClickListener(new EditStorageListenerImpl(this));
    editButton.addStyleName("link");

    Button deleteButton = new Button("delete");
    deleteButton.setData(item);
    deleteButton.addClickListener(new DeleteStorageListenerImpl(this));
    deleteButton.addStyleName("link");

    actionsLayout.addComponent(viewButton);
    actionsLayout.addComponent(editButton);
    actionsLayout.addComponent(deleteButton);

    return actionsLayout;
  }

  private void setSelectedStorage(StorageRepresentation selectedStorage) {
    this.selectedStorage = selectedStorage;
    refreshDetailsPanel();
  }

  /**
   * Updates the metadata panel with new content.
   */
  private void refreshDetailsPanel() {
    VerticalLayout panelContent = new VerticalLayout();

    Storage item = selectedStorage.getItem();
    List<Link> links = selectedStorage.getLinks();

    panelContent.setMargin(true);

    Map<String, String> options = item.getOptions();

    for (Map.Entry<String, String> entry : options.entrySet()) {
      panelContent.addComponent(new DefinedLabel(entry.getKey(), entry.getValue()));
    }

    storageDetailsPanel.setContent(panelContent);
  }


  /**
   * Selection listener for changing the storage selection.
   */
  private final class ViewStorageListenerImpl implements Button.ClickListener {

    /**
     * Parent view to which the event is cascaded.
     */
    private final StoragePanel storagePanel;

    /**
     * @param storagePanel parent view.
     */
    private ViewStorageListenerImpl(final StoragePanel storagePanel) {
      this.storagePanel = storagePanel;
    }

    @Override
    public void buttonClick(final Button.ClickEvent event) {
      StorageRepresentation data = (StorageRepresentation) event.getButton().getData();
      storagePanel.setSelectedStorage(data);
    }
  }

  /**
   * Edit listener for changing the storage object.
   */
  private final class EditStorageListenerImpl implements Button.ClickListener {

    /**
     * Parent view to which the event is cascaded.
     */
    private final StoragePanel storagePanel;

    /**
     * @param storagePanel parent view.
     */
    private EditStorageListenerImpl(final StoragePanel storagePanel) {
      this.storagePanel = storagePanel;
    }

    @Override
    public void buttonClick(final Button.ClickEvent event) {

      StorageRepresentation data = (StorageRepresentation) event.getButton().getData();
      Storage storage = data.getItem();
      try {
        StorageCreateWindow storageCreateWindow =
            new StorageCreateWindow(restClientService, storage);
        storageCreateWindow.addCloseListener(new Window.CloseListener() {
          @Override
          public void windowClose(Window.CloseEvent e) {
            storagePanel.updateStorageTable();
          }
        });
        UI.getCurrent().addWindow(storageCreateWindow);

      } catch (JsonProcessingException ex) {
        LOGGER.error("Unable to parse storage. Error message: " + ex.getMessage());
        com.vaadin.ui.Notification.show("Update failed: " + ex.getMessage());
      }
    }
  }


  private final class DeleteStorageListenerImpl implements Button.ClickListener {

    /**
     * Parent view to which the event is cascaded.
     */
    private final StoragePanel storagePanel;

    /**
     * @param storagePanel parent view.
     */
    private DeleteStorageListenerImpl(final StoragePanel storagePanel) {
      this.storagePanel = storagePanel;
    }

    @Override
    public void buttonClick(final Button.ClickEvent event) {
      StorageRepresentation data = (StorageRepresentation) event.getButton().getData();
      Storage storage = data.getItem();

      ConfirmDialog.show(UI.getCurrent(), "Warning",
          "Are you sure that you want to delete storage \"" + storage.getName() + "\"?", "Yes",
          "No", new ConfirmDialog.Listener() {
            public void onClose(ConfirmDialog dialog) {
              if (dialog.isConfirmed()) {
                try {
                  restClientService.deleteStorage(storage.getId());
                  storagePanel.updateStorageTable();
                } catch (Exception e) {
                  LOGGER.error(String.format(
                      "Unable to delete storage with id:  %s. Error message: %s", storage.getId(),
                      e.getMessage()));
                  com.vaadin.ui.Notification.show("Unable to delete: " + e.getMessage());
                }

              }
            }
          });
    }
  }
}
