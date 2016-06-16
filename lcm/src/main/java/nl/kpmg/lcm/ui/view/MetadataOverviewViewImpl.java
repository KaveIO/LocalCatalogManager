/*
 * Copyright 2016 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.kpmg.lcm.ui.view;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ws.rs.core.Link;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.rest.client.version0.types.MetaDataRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.MetaDatasRepresentation;
import nl.kpmg.lcm.ui.component.DefinedLabel;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author mhoekstra
 */
@Component
@VaadinView(MetadataOverviewViewImpl.VIEW_NAME)
public class MetadataOverviewViewImpl extends VerticalLayout implements MetadataOverviewView {

    /**
     * The linkable name of this view.
     */
    public static final String VIEW_NAME = "metadata-overview";

    /**
     * The default size of the side panels of this view.
     */
    private static final String PANEL_SIZE = "400px";

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

    /**
     * Side Panel filled with details of the currently selected metadata item.
     */
    private final Panel metadataPanel = new Panel("Metadata details");

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
        final HorizontalLayout root = new HorizontalLayout();

        table.addContainerProperty("Name", String.class, null);
        table.addContainerProperty("Location", String.class, null);
        table.addContainerProperty("Actions", Button.class, null);

        table.setWidth("100%");
        table.setHeight("100%");
        metadataPanel.setWidth(PANEL_SIZE);
        metadataPanel.setHeight("100%");

        root.addComponent(table);
        root.addComponent(metadataPanel);

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
        try {
            items = restClientService.getLocalMetadata();

            for (MetaDataRepresentation item : items.getItems()) {
                MetaData metaData = item.getItem();
                List<Link> links = item.getLinks();

                Button viewButton = new Button("view");
                viewButton.setData(item);
                viewButton.addClickListener(new SelectMetadataListenerImpl(this));
                viewButton.addStyleName("link");

                table.addItem(new Object[] {
                        metaData.getName(),
                        metaData.getDataUri(),
                        viewButton
                    }, metaData.getName());
            }
        } catch (AuthenticationException ex) {
            getUI().getNavigator().navigateTo("");
        }
    }

    /**
     * Sets the selected metadata.
     *
     * @param metaDataRepresentation to set
     */
    @Override
    public final void setSelectedMetadata(final MetaDataRepresentation metaDataRepresentation) {
        this.metaDataRepresentation = metaDataRepresentation;
        updateSelectedMetadata();
    }

    /**
     * Updates the metadata panel with new content.
     */
    private void updateSelectedMetadata() {
        VerticalLayout panelContent = new VerticalLayout();

        MetaData item = metaDataRepresentation.getItem();
        List<Link> links = metaDataRepresentation.getLinks();

        panelContent.setMargin(true);
        if (item.getName() != null) {
            panelContent.addComponent(new DefinedLabel("Name", item.getName()));
        }
        if (item.get("general.owner") != null) {
            panelContent.addComponent(new DefinedLabel("Owner", (String) item.get("general.owner")));
        }
        if (item.get("general.description") != null) {
            panelContent.addComponent(new DefinedLabel("Description", (String) item.get("general.description")));
        }

        metadataPanel.setContent(panelContent);
    }

    /**
     * Selection listener for changing the metadata selection.
     */
    private final class SelectMetadataListenerImpl implements Button.ClickListener {

        /**
         * Parent view to which the event is cascaded.
         */
        private final MetadataOverviewView metadataOverviewView;

        /**
         * @param metadataOverviewView parent view.
         */
        private SelectMetadataListenerImpl(final MetadataOverviewView metadataOverviewView) {
            this.metadataOverviewView = metadataOverviewView;
        }

        @Override
        public void buttonClick(final Button.ClickEvent event) {
            MetaDataRepresentation data = (MetaDataRepresentation) event.getButton().getData();
            metadataOverviewView.setSelectedMetadata(data);
        }
    }
}
