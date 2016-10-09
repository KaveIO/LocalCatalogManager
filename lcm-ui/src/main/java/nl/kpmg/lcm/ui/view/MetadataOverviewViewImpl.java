/**
 * Copyright 2016 KPMG N.V.(unless otherwise stated).**Licensed under the Apache License,Version
 * 2.0(the"License");you may not use this file/except*in compliance with the License.You may obtain
 * a copy of the License at**http:// www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing,software distributed under the/License*is distributed on
 * an"AS IS"BASIS,WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either/express*or implied.See the
 * License for the specific language governing permissions and limitations/under*the License.
 */

package nl.kpmg.lcm.ui.view;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import nl.kpmg.lcm.client.ClientException;
import nl.kpmg.lcm.rest.types.MetaDataRepresentation;
import nl.kpmg.lcm.rest.types.MetaDatasRepresentation;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.metadata.MetadataCreateWindow;
import nl.kpmg.lcm.ui.view.metadata.MetadataEditWindow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Link;

import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author mhoekstra
 */
@Component
@VaadinView(MetadataOverviewViewImpl.VIEW_NAME)
public class MetadataOverviewViewImpl extends VerticalLayout
    implements MetadataOverviewView, Button.ClickListener {

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
  private final Table table = new Table();

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
        List<Link> links = item.getLinks();

        Button viewButton = new Button("view");
        viewButton.setData(item);
        viewButton.addClickListener(new ViewButtonClickListener());
        viewButton.addStyleName("link");

        MetaData metaData = item.getItem();
        table.addItem(new Object[] {metaData.getName(), metaData.getDataUri(), viewButton},
            metaData.getName());
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
