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

package nl.kpmg.lcm.ui.view.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.common.rest.types.MetaDataRepresentation;
import nl.kpmg.lcm.ui.component.DefinedLabel;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.DataCreationException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.MetadataOverviewView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.ws.rs.core.Link;

/**
 *
 * @author mhoekstra
 */
public class MetadataEditWindow extends Window implements Button.ClickListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataEditWindow.class.getName());
  /**
   * The default size of the side panels of this view.
   */
  private static final String PANEL_SIZE = "900px";

  private static final String PANEL_HEIGHT = "500px";

  private static final int MAX_NUMBER_OF_ROWS_FOR_RAW_PANEL = 15;

  /**
   * The default size of the side panels of this view.
   */
  private static final String DEFAULT_TITLE = "Edit Metadata";

  private final RestClientService restClientService;

  private final MetadataOverviewView metadataOverviewView;

  private final MetaDataRepresentation metaDataRepresentation;

  private final MetaData metadata;

  private TextArea textArea;

  private Button saveButton;

  private Button deleteButton;

  public MetadataEditWindow(RestClientService restClientService,
      MetaDataRepresentation metaDataRepresentation) {
    this(restClientService, metaDataRepresentation, null);
  }

  public MetadataEditWindow(RestClientService restClientService,
    MetaDataRepresentation metaDataRepresentation, MetadataOverviewView metadataOverviewView) {
    super(DEFAULT_TITLE);
    this.restClientService = restClientService;
    this.metaDataRepresentation = metaDataRepresentation;
    this.metadata = metaDataRepresentation.getItem();
    this.metadataOverviewView = metadataOverviewView;
    init();
  }

  private void init() {
    Layout viewPanel = initViewPanel();
    Layout rawPanel = initRawPanel();
    Layout adminPanel = initAdministrationPanel();

    TabSheet tabsheet = new TabSheet();
    tabsheet.addTab(viewPanel, "View");
    tabsheet.addTab(rawPanel, "Raw");
    tabsheet.addTab(adminPanel, "Administration");

    this.setWidth(PANEL_SIZE);
    this.setHeight(PANEL_HEIGHT);
    this.setModal(true);
    this.setContent(tabsheet);
  }


  private Layout initViewPanel() {
    VerticalLayout panelContent = new VerticalLayout();

    MetaDataWrapper item = new MetaDataWrapper(metaDataRepresentation.getItem());
    List<Link> links = metaDataRepresentation.getLinks();

    panelContent.setMargin(true);
    addDefinedLabel(panelContent, "Name", item.getName());
    //TODO refactore this uri list can not be listed here!
    addDefinedLabel(panelContent, "Location", item.getData().getUri().get(0));
    addDefinedLabel(panelContent, "Owner", item.getGeneralInfo().getOwner());
    addDefinedLabel(panelContent, "Description", item.getGeneralInfo().getDescription());

    return panelContent;
  }

  private Layout initRawPanel() {
    textArea = new TextArea();
    textArea.setWidth("100%");
    textArea.setHeight("100%");
    textArea.setRows(MAX_NUMBER_OF_ROWS_FOR_RAW_PANEL);

    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String rawMetadata = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metadata);
      textArea.setValue(rawMetadata);
    } catch (JsonProcessingException ex) {
      textArea.setEnabled(false);
      LOGGER.warn("Couldn't serialize metadata object", ex);
    }

    saveButton = new Button("Save");
    saveButton.addClickListener(this);

    VerticalLayout panelContent = new VerticalLayout();
    panelContent.setMargin(true);
    panelContent.setSpacing(true);
    panelContent.addComponent(textArea);
    panelContent.addComponent(saveButton);

    return panelContent;
  }

  private Layout initAdministrationPanel() {
    Label warning = new Label(
        "These are unprotected actions with non-reversable consequences. Please be careful.");

    deleteButton = new Button("Delete");
    deleteButton.addClickListener(this);

    VerticalLayout panelContent = new VerticalLayout();
    panelContent.setMargin(true);
    panelContent.setSpacing(true);
    panelContent.addComponent(warning);
    panelContent.addComponent(deleteButton);

    return panelContent;
  }

  @Override
  public void buttonClick(Button.ClickEvent event) {
    if (event.getSource() == saveButton) {
      try {
        String rawMetadata = textArea.getValue();
        restClientService.putMetadata(metadata.getId(), rawMetadata);
        Notification.show("Edit of metadata successful.");
        if (metadataOverviewView != null) {
          metadataOverviewView.updateContent();
        }
        this.close();
      } catch (ServerException | DataCreationException | AuthenticationException ex) {
        Notification.show("Edit of metadata failed.");
        LOGGER.warn("Edit of metadata failed." + ex.getMessage());
      }
    } else if (event.getSource() == deleteButton) {
      try {
        restClientService.deleteMetadata(metadata.getId());
        Notification.show("Delete of metadata successful.");
        if (metadataOverviewView != null) {
          metadataOverviewView.updateContent();
        }
        this.close();
      } catch (ServerException | DataCreationException | AuthenticationException ex) {
        Notification.show("Edit of metadata failed.");
        LOGGER.warn("Edit of metadata failed." + ex.getMessage());
      }
    }
  }

  private void addDefinedLabel(VerticalLayout panelContent, String title, String content) {
    if (content != null) {
      panelContent.addComponent(new DefinedLabel(title, content));
    }
  }
}
