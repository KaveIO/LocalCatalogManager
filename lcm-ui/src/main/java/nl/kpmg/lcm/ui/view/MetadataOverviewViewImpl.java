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

package nl.kpmg.lcm.ui.view;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import nl.kpmg.lcm.client.ClientException;
import nl.kpmg.lcm.rest.types.MetaDataRepresentation;
import nl.kpmg.lcm.rest.types.MetaDatasRepresentation;
import nl.kpmg.lcm.server.Constants;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.metadata.MetadataCreateWindow;
import nl.kpmg.lcm.ui.view.metadata.MetadataEditWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import javax.annotation.PostConstruct;

/**
 *
 * @author mhoekstra
 */
@Component
@SpringView(name = MetadataOverviewViewImpl.VIEW_NAME)
public class MetadataOverviewViewImpl extends VerticalLayout
    implements MetadataOverviewView, Button.ClickListener {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(MetadataOverviewViewImpl.class.getName());

  /**
   * The linkable name of this view.
   */
  public static final String VIEW_NAME = "metadata-overview";

  /**
   * The service for interacting with the backend.
   */
  @Autowired
  private RestClientService restClientService;

  /**
   * The auto wired main UI component.
   */
  @Autowired
  private UI ui;

  /**
   * Main UI table containing the current list of metadata items.
   */
  private final TreeTable table = new TreeTable();

  private final Button createButton = new Button("Create");

  private final Button refreshButton = new Button("Refresh");

  /**
   * The list of metadata items fetched from the service.
   */
  private MetaDatasRepresentation items;

  /**
   * Currently selected metadata item.
   */
  private MetaDataRepresentation metaDataRepresentation;

  /**
   * Builds the interface.
   */
  @PostConstruct
  public final void init() {
    final VerticalLayout root = new VerticalLayout();

    createButton.addClickListener(this);
    refreshButton.addClickListener(this);

    HorizontalLayout menubar = new HorizontalLayout();
    createButton.addStyleName("margin-10");
    refreshButton.addStyleName("margin-10");
    menubar.addComponent(createButton);
    menubar.addComponent(refreshButton);

    table.addContainerProperty("Name", String.class, null);
    table.addContainerProperty("Location", String.class, null);
    table.addContainerProperty("Actions", Button.class, null);

    table.setWidth("100%");
    table.setHeight("100%");

    root.addComponent(menubar);
    root.addComponent(table);

    root.setSpacing(true);
    root.setMargin(true);
    root.setWidth("100%");
    root.setExpandRatio(table, 1f);

    addComponent(root);
  }

  /**
   * Loads the data on presentation.
   *
   * @param event fired when the view is entered.
   */
  @Override
  public final void enter(final ViewChangeListener.ViewChangeEvent event) {
    refreshMetadataOverview();
  }

  @Override
  public void buttonClick(Button.ClickEvent event) {
    if (event.getSource() == createButton) {
      MetadataCreateWindow metadataCreateWindow = new MetadataCreateWindow(restClientService);
      UI.getCurrent().addWindow(metadataCreateWindow);
    } else if (event.getSource() == refreshButton) {
      refreshMetadataOverview();
    }
  }

  private void refreshMetadataOverview() {
    table.removeAllItems();
    try {
      items = restClientService.getLocalMetadata();

      for (MetaDataRepresentation item : items.getItems()) {
        MetaDataWrapper metaDataWrapper;
        try {
          metaDataWrapper = new MetaDataWrapper(item.getItem());
        } catch (LcmValidationException lve) {
          LOGGER.warn("Unable to create wrapper around metadata. Message: " + lve.getMessage());
          continue;
        }
        Button viewButton = new Button("view");
        viewButton.setData(item);
        viewButton.addClickListener(new ViewButtonClickListener());
        viewButton.addStyleName("link");

        addPathToTable(metaDataWrapper.getData().getPath());

        table.addItem(new Object[] {metaDataWrapper.getName(), metaDataWrapper.getData().getUri(),
            viewButton}, metaDataWrapper.getId());
        table.setChildrenAllowed(metaDataWrapper.getId(), false);
        table.setParent(metaDataWrapper.getId(), metaDataWrapper.getData().getPath());
      }
    } catch (AuthenticationException ex) {
      getUI().getNavigator().navigateTo("");
    } catch (ServerException se) {
      Notification.show("Cannot instantiate client HTTPS endpoint");
      getUI().getNavigator().navigateTo("");
    } catch (ClientException ex) {
      Notification.show("Couldn't fetch remote data.");
    }
  }

  /**
   * Recursive method for adding the path to the TreeTable. This method will walk up a path and add
   * non existing nodes to the tree.
   * 
   * @param path full path to add to the tree
   */
  private void addPathToTable(String path) {
    if (path == null || path.isEmpty() || table.containsId(path)) {
      return;
    }

    String[] split = path.split(Constants.NAMESPACE_SEPARATOR);

    table.addItem(new Object[] {split[split.length - 1], null, null}, path);

    if (split.length > 1) {
      String parent = String.join(Constants.NAMESPACE_SEPARATOR,
          Arrays.copyOfRange(split, 0, split.length - 1));
      addPathToTable(parent);
      table.setParent(path, parent);
    }
  }

  private class ViewButtonClickListener implements Button.ClickListener {

    public ViewButtonClickListener() {}

    @Override
    public void buttonClick(Button.ClickEvent event) {
      MetadataEditWindow metadataEditWindow = new MetadataEditWindow(restClientService,
          (MetaDataRepresentation) event.getButton().getData());
      UI.getCurrent().addWindow(metadataEditWindow);
    }
  }
}
