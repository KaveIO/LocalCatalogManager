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
package nl.kpmg.lcm.ui.view.administration.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.DataCreationException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.DynamicDataContainer;

import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 *
 * @author mhoekstra
 */
public class StorageCreateWindow extends Window implements Button.ClickListener {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StorageCreateWindow.class
      .getName());
  /**
   * The default size of the side panels of this view.
   */
  private static final String PANEL_SIZE = "600px";

  /**
   * The default size of the side panels of this view.
   */
  private static final String CREATE_TITLE = "Create Storage";
  private static final String EDIT_TITLE = "Edit Storage";

  private RestClientService restClientService;
  private DynamicDataContainer dataContainer;

  private final TextArea optionsArea = new TextArea("Options");
  private final TextArea credentialsArea = new TextArea("Credentials");
  private final TextArea enrichmentArea = new TextArea("Enrichment options");
  private final TextField nameField = new TextField("Name");
  private final TextField typeField = new TextField("Type");
  private final Button saveButton = new Button("Save");
  private Storage storage;
  private final static int MAX_LENGTH = 128;

  private boolean isCreateOpereration;

  public StorageCreateWindow(RestClientService restClientService, DynamicDataContainer dataContainer) {
    super(CREATE_TITLE);
    isCreateOpereration = true;
    this.restClientService = restClientService;
    this.dataContainer = dataContainer;
    init();
  }

  public StorageCreateWindow(RestClientService restClientService, Storage storage,
      DynamicDataContainer dataContainer) throws JsonProcessingException {
    super(EDIT_TITLE);
    isCreateOpereration = false;
    this.restClientService = restClientService;
    this.dataContainer = dataContainer;
    nameField.setValue(storage.getName());
    typeField.setValue(storage.getType());
    String storageJson = new ObjectMapper().writeValueAsString(storage.getOptions());
    optionsArea.setValue(storageJson);
    if (storage.getEnrichmentProperties() != null) {
      String enrichmentJson =
          new ObjectMapper().writeValueAsString(storage.getEnrichmentProperties());
      enrichmentArea.setValue(enrichmentJson);
    }

    this.storage = storage;
    init();
  }

  private void init() {
    optionsArea.setWidth("100%");
    optionsArea.setHeight("100%");
    credentialsArea.setWidth("100%");
    credentialsArea.setHeight("100%");
    enrichmentArea.setWidth("100%");
    enrichmentArea.setHeight("100%");
    saveButton.addClickListener(this);

    FormLayout panelContent = new FormLayout();
    panelContent.setMargin(true);
    panelContent.addComponent(nameField);
    panelContent.addComponent(typeField);
    panelContent.addComponent(optionsArea);
    panelContent.addComponent(credentialsArea);
    panelContent.addComponent(enrichmentArea);
    panelContent.addComponent(saveButton);

    this.setWidth(PANEL_SIZE);
    this.setModal(true);

    this.setContent(panelContent);
  }

  @Override
  public void buttonClick(Button.ClickEvent event) {
    if (event.getSource() == saveButton) {
      nl.kpmg.lcm.common.validation.Notification notification = new nl.kpmg.lcm.common.validation.Notification();

      validate(notification);
      ObjectMapper mapper = new ObjectMapper();
      JsonNode options = null;
      try {
        options = mapper.readTree(optionsArea.getValue());
      } catch (IOException ex) {
        notification.addError("Options field is not valid Json", ex);
      }

      JsonNode credentials = null;
      try {
        if (credentialsArea.getValue() != null && credentialsArea.getValue().length() > 0) {
          credentials = mapper.readTree(credentialsArea.getValue());
        }
      } catch (IOException ex) {
        notification.addError("Credentials field is not valid Json", ex);
      }

      JsonNode enrichment = null;
      try {
        if (enrichmentArea.getValue() != null && enrichmentArea.getValue().length() > 0) {
          enrichment = mapper.readTree(enrichmentArea.getValue());
        }
      } catch (IOException ex) {
        notification.addError("Enrichment properties are not valid Json", ex);
      }

      if (notification.hasErrors()) {
        Notification.show("Validation failed: " + notification.errorMessage());
        LOGGER.debug("Validation failed: " + notification.errorMessage());
        return;
      }
      // TODO refactore this: use directly storage isntead of json
      ObjectNode rootNode = mapper.createObjectNode();
      rootNode.set("options", options);
      if (credentials != null) {
        rootNode.set("credentials", credentials);
      }
      if (enrichment != null) {
        rootNode.set("enrichment-properties", enrichment);
      }
      rootNode.put("name", nameField.getValue());
      rootNode.put("type", typeField.getValue());

      try {
        if (storage != null) {
          rootNode.put("id", storage.getId());
          rootNode.put("status", storage.getStatus());
          restClientService.updateStorage(rootNode.toString());
          Notification.show("Update was successful.");
        } else {
          restClientService.createStorage(rootNode.toString());
          Notification.show("Creation of storage successful.");
        }
        dataContainer.updateContent();
        this.close();
      } catch (ServerException | DataCreationException | AuthenticationException | IOException ex) {
        Notification.show("Creation of storage failed.");
        LOGGER.warn("Creation of storage failed.", ex);
      }
    }
  }

  private void validate(nl.kpmg.lcm.common.validation.Notification notification) {
    validateText(nameField, notification);
    validateText(typeField, notification);
    if (optionsArea.getValue().isEmpty()) {
      notification.addError(optionsArea.getCaption() + " can not be empty");
    }
  }

  private void validateText(TextField field, nl.kpmg.lcm.common.validation.Notification notification) {
    if (field.getValue().isEmpty()) {
      notification.addError(field.getCaption() + " can not be empty");
    }

    if (field.getValue().indexOf(' ') != -1) {
      notification.addError(field.getCaption() + " can not contain spaces!");
    }

    if (field.getValue().length() > MAX_LENGTH) {
      notification.addError(field.getCaption() + " is too long! Max length : " + MAX_LENGTH);
    }
  }
}
