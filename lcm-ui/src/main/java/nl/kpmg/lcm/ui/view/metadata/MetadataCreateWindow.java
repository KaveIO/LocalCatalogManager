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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.metadata.DataDescriptor;
import nl.kpmg.lcm.server.data.metadata.DynamicDataDescriptor;
import nl.kpmg.lcm.server.data.metadata.EnrichmentPropertiesDescriptor;
import nl.kpmg.lcm.server.data.metadata.GeneralInfoDescriptor;
import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.server.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.DataCreationException;
import nl.kpmg.lcm.ui.rest.RestClientService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 *
 * @author mhoekstra
 */
public class MetadataCreateWindow extends Window implements Button.ClickListener {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetadataCreateWindow.class.getName());
  /**
   * The default size of the side panels of this view.
   */
  private static final String PANEL_WIDTH = "600px";
  private static final String PANEL_HEIGTH = "670px";

  /**
   * The default size of the side panels of this view.
   */
  private static final String DEFAULT_TITLE = "Create Metadata";

  private RestClientService restClientService;

  private TextArea rawView;
  private TextArea dataView;
  private TextArea generalInfoView;
  private TextArea enrichmentPropertiesView;
  private TextArea dynamicDataView;
  private TextField nameField;

  private VerticalLayout rawPanel = new VerticalLayout();
  private FormLayout sectionPanel = new FormLayout();
  private TabSheet tabsheet;

  private Button saveButton;

  public MetadataCreateWindow(RestClientService restClientService) {
    super(DEFAULT_TITLE);
    this.restClientService = restClientService;
    init();
  }

  private void init() {
    tabsheet = new TabSheet();

    sectionPanel = initSectionPanel();
    tabsheet.addTab(sectionPanel, "Section view");

    rawPanel = initRawPanel();
    tabsheet.addTab(rawPanel, "Raw view");
    tabsheet.addSelectedTabChangeListener((event) -> {
      if (event.getTabSheet().getSelectedTab() == rawPanel) {
        try {
          updateRawView();
        } catch (Property.ReadOnlyException | IOException ex) {
          Notification.show("Unable to merge metadata sections. Raw metadata is not updated!");
        }
      } else if (event.getTabSheet().getSelectedTab() == sectionPanel) {
        updateSectionView();
      }
    });

    this.setWidth(PANEL_WIDTH);
    this.setModal(true);

    FormLayout mainPanel = new FormLayout();
    mainPanel.addComponent(tabsheet);

    saveButton = new Button("Save");
    saveButton.addClickListener(this);
    mainPanel.addComponent(saveButton);

    this.setContent(mainPanel);
  }

  private FormLayout initSectionPanel() {
    nameField = new TextField("Name");
    nameField.setWidth("100%");
    sectionPanel.addComponent(nameField);

    dataView = new TextArea("Data");
    dataView.setWidth("100%");
    sectionPanel.addComponent(dataView);

    generalInfoView = new TextArea("General Info");
    generalInfoView.setWidth("100%");
    sectionPanel.addComponent(generalInfoView);

    enrichmentPropertiesView = new TextArea("Enrichment Properties");
    enrichmentPropertiesView.setWidth("100%");
    sectionPanel.addComponent(enrichmentPropertiesView);

    dynamicDataView = new TextArea("Dynamic data");
    dynamicDataView.setWidth("100%");
    sectionPanel.addComponent(dynamicDataView);

    sectionPanel.setMargin(true);
    sectionPanel.setHeight(PANEL_HEIGTH);
    return sectionPanel;
  }

  private VerticalLayout initRawPanel() {
    rawView = new TextArea("Raw metadata");
    rawView.setWidth("100%");
    rawView.setHeight("100%");

    rawPanel.setMargin(true);
    rawPanel.addComponent(rawView);
    rawPanel.setHeight(PANEL_HEIGTH);
    return rawPanel;
  }

  @Override
  public void buttonClick(Button.ClickEvent event) {
    if (event.getSource() == saveButton) {
      try {
        if (tabsheet.getSelectedTab() == sectionPanel) {
          updateRawView();
        }
        String metadata = rawView.getValue();
        ObjectMapper objectMapper = new ObjectMapper();
        Map metadataMap = objectMapper.readValue(metadata, Map.class);
        new MetaDataWrapper(new MetaData(metadataMap));
        restClientService.postMetadata(metadata);
        Notification.show("Creation of metadata successful.");
        this.close();
      } catch (ServerException | DataCreationException | AuthenticationException ex) {
        Notification.show("Creation of metadata failed.");
        LOGGER.warn("Creation of metadata failed.", ex.getMessage());
      } catch (LcmValidationException | IOException ex) {
        Notification.show("Creation of metadata failed. Invalid metadata!");
        LOGGER.warn("Creation of metadata failed. Invalid metadata. Message: ", ex.getMessage());
      }
    }
  }

  class SectionChangedListener implements Property.ValueChangeListener {
    @Override
    public void valueChange(Property.ValueChangeEvent event) {

    }
  }

  private void updateRawView() throws Property.ReadOnlyException, IOException {
    MetaDataWrapper metadata = new MetaDataWrapper();

    if (enrichmentPropertiesView.getValue() != null
        && !enrichmentPropertiesView.getValue().isEmpty()) {
      Map enrichmentPropertiesMap;
      enrichmentPropertiesMap = getDescriptor(enrichmentPropertiesView.getValue());
      EnrichmentPropertiesDescriptor enrichmentPropertiesDescriptor =
          new EnrichmentPropertiesDescriptor(enrichmentPropertiesMap);
      metadata.setEnrichmentProperties(enrichmentPropertiesDescriptor);
    }

    if (dynamicDataView.getValue() != null && !dynamicDataView.getValue().isEmpty()) {
      Map dynamicDataMap = getDescriptor(dynamicDataView.getValue());
      DynamicDataDescriptor dynamicData = new DynamicDataDescriptor(dynamicDataMap);
      metadata.setDynamicData(dynamicData);
    }

    if (dataView.getValue() != null && !dataView.getValue().isEmpty()) {
      Map dataMap = getDescriptor(dataView.getValue());
      DataDescriptor dataDescriptor = new DataDescriptor(dataMap);
      metadata.setData(dataDescriptor);
    }

    if (generalInfoView.getValue() != null && !generalInfoView.getValue().isEmpty()) {
      Map generalInfoMap = getDescriptor(generalInfoView.getValue());
      GeneralInfoDescriptor generalInfoDescriptor = new GeneralInfoDescriptor(generalInfoMap);
      metadata.setGeneralInfo(generalInfoDescriptor);
    }

    if (nameField.getValue() != null && !nameField.getValue().isEmpty()) {
      metadata.setName(nameField.getValue());
    }

    if (!metadata.isEmpty()) {
      Gson gson = new Gson();
      String metadataJson = gson.toJson(metadata.getMetaData().getInnerMap());
      rawView.setValue(metadataJson);
    }
    return;
  }

  private Map getDescriptor(String jsonAsString) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(jsonAsString, Map.class);
  }

  public void updateSectionView() {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      Gson gson = new Gson();
      Map metadataMap = objectMapper.readValue(rawView.getValue(), Map.class);
      MetaDataWrapper metaData = new MetaDataWrapper(new MetaData(metadataMap));
      if (metaData.getData() != null && metaData.getData().getMap() != null
          && metaData.getData().getMap().size() > 0) {
        String dataJson = gson.toJson(metaData.getData().getMap());
        dataView.setValue(dataJson);
      }

      if (metaData.getDynamicData() != null && metaData.getDynamicData().getMap() != null
          && metaData.getDynamicData().getMap().size() > 0) {
        String dynamicDataJson = gson.toJson(metaData.getDynamicData().getMap());
        dynamicDataView.setValue(dynamicDataJson);
      }

      if (metaData.getGeneralInfo() != null && metaData.getGeneralInfo().getMap() != null
          && metaData.getGeneralInfo().getMap().size() > 0) {
        String generalInfoJson = gson.toJson(metaData.getGeneralInfo().getMap());
        generalInfoView.setValue(generalInfoJson);
      }

      if (metaData.getEnrichmentProperties() != null
          && metaData.getEnrichmentProperties().getMap() != null
          && metaData.getEnrichmentProperties().getMap().size() > 0) {
        String enrichmentPropertiesJson = gson.toJson(metaData.getEnrichmentProperties().getMap());
        enrichmentPropertiesView.setValue(enrichmentPropertiesJson);
      }

      nameField.setValue(metaData.getName());
    } catch (IOException | LcmValidationException e) {
      LOGGER.debug("Unable to parse metadata. Message: " + e.getMessage());
      Notification.show("The raw metadata is not valid!");
    }
  }
}
