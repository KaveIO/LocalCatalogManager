/**
 * Copyright 2016 KPMG N.V.(unless otherwise stated).**Licensed under the Apache License,Version
 * 2.0(the"License");*you may not use this file except in compliance with the License.*You may
 * obtain a copy of the License at**http:// www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing,software*distributed under the License is distributed on
 * an"AS IS"BASIS,*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.*See the
 * License for the specific language governing permissions and*limitations under the License.
 */
package nl.kpmg.lcm.ui.view.administration;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import nl.kpmg.lcm.rest.types.StorageRepresentation;
import nl.kpmg.lcm.rest.types.StoragesRepresentation;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Link;

import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.ui.component.DefinedLabel;

/****
 *
 * @author mhoekstra
 */
public class StoragePanel extends CustomComponent {

  /**
   * The default size of the side panels of this view.
   */
  private static final String PANEL_SIZE = "400px";

  private StoragesRepresentation storages;
  private final Table storageTable;
  private final Panel storageDetailsPanel = new Panel("Storage details");
  private StorageRepresentation selectedStorage;

  public StoragePanel() {
    Panel panel = new Panel();

    HorizontalLayout root = new HorizontalLayout();

    Panel storageOverviewPanel = new Panel();

    VerticalLayout storagePanelLayout = new VerticalLayout();
    storageTable = new Table();
    storageTable.setWidth("100%");
    storageTable.addContainerProperty("Name", String.class, null);
    storageTable.addContainerProperty("Type", String.class, "file");
    storageTable.addContainerProperty("Status", String.class, null);
    storageTable.addContainerProperty("Actions", Button.class, null);

    Label storageLabel = new Label("Storage");
    storageLabel.setStyleName("v-label-h2");
    storagePanelLayout.addComponent(storageLabel);
    storagePanelLayout.addComponent(storageTable);
    storageOverviewPanel.setContent(storagePanelLayout);
    storageOverviewPanel.setHeight("100%");

    storageDetailsPanel.setWidth(PANEL_SIZE);
    storageDetailsPanel.setHeight("100%");

    root.addComponent(storageOverviewPanel);
    root.addComponent(storageDetailsPanel);

    root.setSpacing(true);
    root.setMargin(true);
    root.setWidth("100%");
    root.setHeight("100%");
    root.setExpandRatio(storageOverviewPanel, 1f);

    panel.setContent(root);

    setCompositionRoot(panel);
  }

  public void setStorages(StoragesRepresentation storages) {
    this.storages = storages;
    updateStorages();
  }

  private void updateStorages() {
    storageTable.removeAllItems();
    if (storages != null) {
      for (StorageRepresentation item : storages.getItems()) {
        Storage storage = item.getItem();

        Button viewButton = new Button("view");
        viewButton.setData(item);
        viewButton.addClickListener(new SelectStorageListenerImpl(this));
        viewButton.addStyleName("link");

        storageTable.addItem(new Object[] {storage.getId(), "file", "unknown", viewButton},
            storage.getId());
      }
    }
  }

  private void setSelectedStorage(StorageRepresentation selectedStorage) {
    this.selectedStorage = selectedStorage;
    updateSelectedStorage();
  }


  /**
   * Updates the metadata panel with new content.
   */
  private void updateSelectedStorage() {
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
   * Selection listener for changing the metadata selection.
   */
  private final class SelectStorageListenerImpl implements Button.ClickListener {

    /**
     * Parent view to which the event is cascaded.
     */
    private final StoragePanel storagePanel;

    /**
     * @param storagePanel parent view.
     */
    private SelectStorageListenerImpl(final StoragePanel storagePanel) {
      this.storagePanel = storagePanel;
    }

    @Override
    public void buttonClick(final Button.ClickEvent event) {
      StorageRepresentation data = (StorageRepresentation) event.getButton().getData();
      storagePanel.setSelectedStorage(data);
    }
  }
}
