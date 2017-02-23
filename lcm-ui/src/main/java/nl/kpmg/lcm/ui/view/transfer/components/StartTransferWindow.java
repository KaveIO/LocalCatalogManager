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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;

import nl.kpmg.lcm.client.ClientException;
import nl.kpmg.lcm.rest.types.StorageRepresentation;
import nl.kpmg.lcm.rest.types.StoragesRepresentation;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.DataCreationException;
import nl.kpmg.lcm.ui.rest.RestClientService;

import org.slf4j.LoggerFactory;

import java.io.IOException;

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
  private static final String PANEL_SIZE = "600px";

  /**
   * The default size of the side panels of this view.
   */
  private static final String TITLE = "Schedule Data Transfer";

  private RestClientService restClientService;

  private Label remoteLcmLabel;
  private Label metadataIdLabel;
  private Label metadataNameLabel;
  private ComboBox storageListComboBox;
  private String remoteLcmId;
  private String remoteLcmUrl;
  private String remoteMetadataId;
  private final Button startButton = new Button("Start");

  public StartTransferWindow(RestClientService restClientService, String remoteLcmId,
       String remoteLcmUrl, String metadataId, String metadataName) {
    super(TITLE);
    this.restClientService = restClientService;
    this.remoteLcmId = remoteLcmId;
    this.remoteMetadataId = metadataId;
    init(remoteLcmUrl, metadataId, metadataName);
  }

  private void init(String remoteLcmUrl, String metadataId, String metadataName) {
    remoteLcmLabel = new Label();
    remoteLcmLabel.setCaption("Remote LCM");
    remoteLcmLabel.setValue(remoteLcmUrl);
    metadataIdLabel = new Label();
    metadataIdLabel.setCaption("Metadata Id");
    metadataIdLabel.setValue(metadataId);
    metadataNameLabel = new Label();
    metadataNameLabel.setCaption("Metadata Name");
    metadataNameLabel.setValue(metadataName);

    startButton.addClickListener(this);

    FormLayout panelContent = new FormLayout();
    panelContent.setMargin(true);

    storageListComboBox = initStorageListComboBox();
    panelContent.addComponent(remoteLcmLabel);
    panelContent.addComponent(metadataIdLabel);
    panelContent.addComponent(metadataNameLabel);
    panelContent.addComponent(storageListComboBox);
    panelContent.addComponent(startButton);

    this.setWidth(PANEL_SIZE);
    this.setModal(true);

    this.setContent(panelContent);
  }

  private ComboBox initStorageListComboBox() throws UnsupportedOperationException {
    ComboBox storageListComboBox = new ComboBox("Local Storage");
    StoragesRepresentation storages;
    try {
      storages = restClientService.getStorage();

      for (StorageRepresentation item : storages.getItems()) {
        Storage storage = item.getItem();

        String name = storage.getName() + " (" + storage.getType() + ")";
        storageListComboBox.addItem(storage.getId());
        storageListComboBox.setItemCaption(storage.getId(), name);
      }
    } catch (AuthenticationException | ServerException | ClientException ex) {
      LOGGER.error("Unable to load remote LCMs! Message:" + ex.getMessage());
    }
    storageListComboBox.addStyleName("margin-right-20");
    storageListComboBox.addStyleName("width-search-field");
    storageListComboBox.setRequired(true);
    storageListComboBox.setInputPrompt("Please select one");

    return storageListComboBox;
  }

  @Override
  public void buttonClick(Button.ClickEvent event) {
    if (event.getSource() == startButton) {
      nl.kpmg.lcm.validation.Notification notification = new nl.kpmg.lcm.validation.Notification();

      validate(notification);

      if (notification.hasErrors()) {
        Notification.show("Validation failed: " + notification.errorMessage());
        LOGGER.debug("Validation failed: " + notification.errorMessage());
        return;
      }

      ObjectMapper mapper = new ObjectMapper();
      ObjectNode rootNode = mapper.createObjectNode();
      rootNode.put("local-storage-id", (String) storageListComboBox.getValue());

      try {
        restClientService.triggerTransfer(remoteLcmId, remoteMetadataId, rootNode.toString());
        Notification.show("Transfer was scheduled successfully.");
        this.close();
      } catch (ServerException | DataCreationException | AuthenticationException | IOException ex) {
        Notification.show("Unable to schedule stransfer.");
        LOGGER.warn("Unable to schedule stransfer." + ex.getMessage());
      }
    }
  }

  private void validate(nl.kpmg.lcm.validation.Notification notification) {
    String selection = (String) storageListComboBox.getValue();
    if (selection == null || selection.length() == 0) {
      notification.addError("You must select storage!");
    }
  }
}
