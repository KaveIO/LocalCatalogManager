/*
 * Copyright 2017 KPMG N.V. (unless otherwise stated).
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
package nl.kpmg.lcm.ui.view.transfer.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import nl.kpmg.lcm.common.NamespacePathValidator;
import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.TransferSettings;
import nl.kpmg.lcm.common.data.TransferValidator;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.common.rest.types.StorageRepresentation;
import nl.kpmg.lcm.common.rest.types.StoragesRepresentation;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.LcmBadRequestException;
import nl.kpmg.lcm.ui.rest.RestClientService;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 *
 * @author shristov
 */
public class StartTransferWindow extends Window implements Button.ClickListener {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StartTransferWindow.class
      .getName());
  /**
   * The default size of the side panels of this view.
   */
  private static final String DIALOG_WIDTH = "600px";

  private static final String TAB_HEIGHT = "250px";

  /**
   * The default size of the side panels of this view.
   */
  private static final String TITLE = "Schedule Data Transfer";

  private RestClientService restClientService;

  private Label remoteLcmLabel;
  private Label metadataIdLabel;
  private Label metadataNameLabel;
  private Label expirationTimeWarning;
  private ComboBox storageListComboBox;
  private String remoteLcmId;
  private String remoteLcmUrl;
  private MetaDataWrapper remoteMetadata;
  private TextField metadataNameSpaceField;
  private final Button startButton = new Button("Start");

  private ComboBox overwriteComboBox;
  private TextField writeChunkSizeField;
  private TextField varcharSizeField;
  private TextField decimalPrecisionField;
  private final TabSheet tabsheet = new TabSheet();

  public StartTransferWindow(RestClientService restClientService, String remoteLcmId,
      String remoteLcmUrl, MetaData remoteMetadata) {
    super(TITLE);
    this.restClientService = restClientService;
    this.remoteLcmId = remoteLcmId;
    this.remoteMetadata = new MetaDataWrapper(remoteMetadata);
    init(remoteLcmUrl, remoteMetadata.getId(), remoteMetadata.getName());
  }

  private void init(String remoteLcmUrl, String remoteMetadataId, String remoteMetadataName) {
    FormLayout commonContentPanel = initCommonContentPanel(remoteMetadataId, remoteMetadataName);
    tabsheet.addTab(commonContentPanel, "Common");

    FormLayout settingsContent = initSettingsPanel();
    tabsheet.addTab(settingsContent, "Settings");

    startButton.addClickListener(this);
    FormLayout mainPanel = new FormLayout();
    mainPanel.addComponent(tabsheet);

    createExpirationTimeWarning();
    if (expirationTimeWarning != null) {
      mainPanel.addComponent(expirationTimeWarning);
    }

    mainPanel.addComponent(startButton);

    this.setWidth(DIALOG_WIDTH);
    this.setModal(true);

    this.setContent(mainPanel);
  }

  private FormLayout initSettingsPanel() throws UnsupportedOperationException {
    FormLayout settingsContent = new FormLayout();
    overwriteComboBox = new ComboBox("Overwrite existing data");
    overwriteComboBox.addItem("true");
    overwriteComboBox.setItemCaption("true", "true");
    overwriteComboBox.addItem("false");
    overwriteComboBox.setItemCaption("false", "false");
    writeChunkSizeField = new TextField("Write chunck size");
    varcharSizeField = new TextField("Varchar size");
    decimalPrecisionField = new TextField("Decinal precision");
    initSettings();

    settingsContent.addComponent(overwriteComboBox);
    settingsContent.addComponent(writeChunkSizeField);
    settingsContent.addComponent(varcharSizeField);
    settingsContent.addComponent(decimalPrecisionField);
    settingsContent.setHeight(TAB_HEIGHT);
    settingsContent.setMargin(true);
    return settingsContent;
  }

  private FormLayout initCommonContentPanel(String metadataId, String metadataName)
      throws UnsupportedOperationException {
    remoteLcmLabel = new Label();
    remoteLcmLabel.setCaption("Remote LCM");
    remoteLcmLabel.setValue(remoteLcmUrl);
    metadataIdLabel = new Label();
    metadataIdLabel.setCaption("Metadata Id");
    metadataIdLabel.setValue(metadataId);
    metadataNameLabel = new Label();
    metadataNameLabel.setCaption("Metadata Name");
    metadataNameLabel.setValue(metadataName);

    FormLayout commonContentPanel = new FormLayout();
    commonContentPanel.setMargin(true);
    storageListComboBox = initStorageListComboBox();
    metadataNameSpaceField = new TextField("Namespace path");
    commonContentPanel.addComponent(remoteLcmLabel);
    commonContentPanel.addComponent(metadataIdLabel);
    commonContentPanel.addComponent(metadataNameLabel);
    commonContentPanel.addComponent(storageListComboBox);
    commonContentPanel.addComponent(metadataNameSpaceField);
    commonContentPanel.addComponent(startButton);
    commonContentPanel.setMargin(true);
    commonContentPanel.setHeight(TAB_HEIGHT);
    return commonContentPanel;
  }

  private ComboBox initStorageListComboBox() throws UnsupportedOperationException {
    ComboBox storageListComboBox = new ComboBox("Local Storage");
    List<String> validStorageTypes =
        TransferValidator.getValidStorageTypes(remoteMetadata.getSourceType());
    StoragesRepresentation storages;
    try {
      storages = restClientService.getStorage();

      for (StorageRepresentation item : storages.getItems()) {
        Storage storage = item.getItem();
        if (validStorageTypes.contains(storage.getType())) {
          String name = storage.getName() + " (" + storage.getType() + ")";
          storageListComboBox.addItem(storage.getId());
          storageListComboBox.setItemCaption(storage.getId(), name);
        }
      }
    } catch (ServerException ex) {
      Notification.show("Unable to init storage list!");
      LOGGER.error("Unable to init storage list! Message:" + ex.getMessage());
    } catch (AuthenticationException | LcmBadRequestException ex) {
      LOGGER.error("Unable to init storage list!" + ex.getMessage());
      Notification.show("Unable to init storage list! Message: " + ex.getMessage());
    }
    storageListComboBox.addStyleName("margin-right-20");
    storageListComboBox.addStyleName("width-search-field");
    storageListComboBox.setRequired(true);
    storageListComboBox.setInputPrompt("Please select one");

    return storageListComboBox;
  }

  private void initSettings() {
    // we asumethat the new instance will be initialized with default values
    TransferSettings settings = new TransferSettings();
    overwriteComboBox.setValue(String.valueOf(settings.isForceOverwrite()));
    writeChunkSizeField.setValue(String.valueOf(settings.getMaximumInsertedRecordsPerQuery()));
    varcharSizeField.setValue(String.valueOf(settings.getVarCharSize()));
    decimalPrecisionField.setValue(String.valueOf(settings.getDecimalPrecision()));
  }

  @Override
  public void buttonClick(Button.ClickEvent event) {
    if (event.getSource() == startButton) {
      nl.kpmg.lcm.common.validation.Notification notification =
          new nl.kpmg.lcm.common.validation.Notification();

      validate(notification);

      if (notification.hasErrors()) {
        Notification.show("Validation failed: " + notification.errorMessage());
        LOGGER.debug("Validation failed: " + notification.errorMessage());
        return;
      }

      ObjectNode payload = createPayload();
      try {
        restClientService.triggerTransfer(remoteLcmId, remoteMetadata.getId(), payload.toString());
        Notification.show("Transfer was scheduled successfully.");
        this.close();
      } catch (ServerException | IOException ex) {
        Notification.show("Unable to schedule stransfer.");
        LOGGER.warn("Unable to schedule stransfer." + ex.getMessage());
      } catch (LcmBadRequestException | AuthenticationException ex) {
        Notification.show("Unable to schedule stransfer. Message: " + ex.getMessage());
        LOGGER.warn("Unable to schedule stransfer." + ex.getMessage());
      }
    }
  }

  private ObjectNode createPayload() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootNode = mapper.createObjectNode();
    rootNode.put("local-storage-id", (String) storageListComboBox.getValue());
    rootNode.put("namespace-path", (String) metadataNameSpaceField.getValue());
    ObjectMapper objectMapper = new ObjectMapper();
    String value = null;
    try {
      value = objectMapper.writeValueAsString(getTransferSettings());
    } catch (JsonProcessingException ex) {
      LOGGER.warn("Unable to create transfer settings!");
    }
    rootNode.put("transfer-settings", value);
    return rootNode;
  }

  private TransferSettings getTransferSettings() {
    TransferSettings settings = new TransferSettings();
    if (overwriteComboBox.getValue() != null) {
      String stringValue = (String) overwriteComboBox.getValue();
      Boolean value = Boolean.parseBoolean(stringValue);
      settings.setForceOverwrite(value);
    }

    if (writeChunkSizeField.getValue() != null) {
      String value = (String) writeChunkSizeField.getValue();
      settings.setMaximumInsertedRecordsPerQuery(Integer.parseInt(value));
    }

    if (varcharSizeField.getValue() != null) {
      String value = (String) varcharSizeField.getValue();
      settings.setVarCharSize(Integer.parseInt(value));
    }

    if (decimalPrecisionField.getValue() != null) {
      String value = (String) decimalPrecisionField.getValue();
      settings.setDecimalPrecision(Integer.parseInt(value));
    }

    return settings;
  }

  private void validate(nl.kpmg.lcm.common.validation.Notification notification) {
    String selection = (String) storageListComboBox.getValue();
    if (selection == null || selection.isEmpty()) {
      notification.addError("You must select storage!");
    }

    String namespaceSelection = (String) metadataNameSpaceField.getValue();
    if (namespaceSelection == null || namespaceSelection.isEmpty()) {
      notification.addError("You must enter namespace path!");
    }

    NamespacePathValidator validator = new NamespacePathValidator();
    validator.validate(namespaceSelection, notification);
  }

  private void createExpirationTimeWarning() {
    String transferTime = remoteMetadata.getExpirationTime().getTransferExpirationTime();
    if (transferTime != null) {
      long seconds = Long.parseLong(transferTime);
      long milis = seconds * 1000;
      Date date = new Date(milis);

      expirationTimeWarning = new Label("Note: The relevant data will be deleted at: " + date);
      expirationTimeWarning.setStyleName("warning");
      expirationTimeWarning.setWidth("100%");
    }
  }

}