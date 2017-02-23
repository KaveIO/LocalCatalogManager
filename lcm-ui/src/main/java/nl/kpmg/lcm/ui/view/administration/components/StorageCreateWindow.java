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

import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.DataCreationException;
import nl.kpmg.lcm.ui.rest.RestClientService;

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

  private final TextArea optionsArea = new TextArea("Options");
  private final TextField nameField = new TextField("Name");
  private final TextField typeField = new TextField("Type");
  private final Button saveButton = new Button("Save");
  private Storage storage;
  private final static int MAX_LENGTH = 128;

  public StorageCreateWindow(RestClientService restClientService) {
    super(CREATE_TITLE);
    this.restClientService = restClientService;
    init();
  }

  public StorageCreateWindow(RestClientService restClientService, Storage storage) throws JsonProcessingException {
    super(EDIT_TITLE);
    this.restClientService = restClientService;
    nameField.setValue(storage.getName());
    typeField.setValue(storage.getType());
    String json = new ObjectMapper().writeValueAsString(storage.getOptions());
    optionsArea.setValue(json);
    this.storage = storage;
    init();
  }

  private void init() {
    optionsArea.setWidth("100%");
    optionsArea.setHeight("100%");
    saveButton.addClickListener(this);

    FormLayout panelContent = new FormLayout();
    panelContent.setMargin(true);
    panelContent.addComponent(nameField);
    panelContent.addComponent(typeField);
    panelContent.addComponent(optionsArea);
    panelContent.addComponent(saveButton);

    this.setWidth(PANEL_SIZE);
    this.setModal(true);

    this.setContent(panelContent);
  }

  @Override
  public void buttonClick(Button.ClickEvent event) {
    if (event.getSource() == saveButton) {
      nl.kpmg.lcm.validation.Notification notification = new nl.kpmg.lcm.validation.Notification();

      validate(notification);
      ObjectMapper mapper = new ObjectMapper();
      JsonNode options = null;
      try {
        options = mapper.readTree(optionsArea.getValue());
      } catch (IOException ex) {
        notification.addError("Options field is not valid Json", ex);
      }

      if (notification.hasErrors()) {
        Notification.show("Validation failed: " + notification.errorMessage());
        LOGGER.debug("Validation failed: " + notification.errorMessage());
        return;
      }

      ObjectNode rootNode = mapper.createObjectNode();
      rootNode.set("options", options);
      rootNode.put("name", nameField.getValue());
      rootNode.put("type", typeField.getValue());

      try {
        if(storage != null) {
            rootNode.put("id", storage.getId());
            restClientService.updateStorage(rootNode.toString());
            Notification.show("Update was successful.");
        } else {
            restClientService.createStorage(rootNode.toString());
            Notification.show("Creation of storage successful.");
        }
        this.close();
      } catch (ServerException | DataCreationException | AuthenticationException | IOException ex) {
        Notification.show("Creation of storage failed.");
        LOGGER.warn("Creation of storage failed.", ex);
      }
    }
  }

  private void validate(nl.kpmg.lcm.validation.Notification notification) {
    validateText(nameField, notification);
    validateText(typeField, notification);
    if (optionsArea.getValue().isEmpty()) {
      notification.addError(optionsArea.getCaption() + " can not be empty");
    }
  }

  private void validateText(TextField field, nl.kpmg.lcm.validation.Notification notification) {
    if (field.getValue().isEmpty()) {
      notification.addError(field.getCaption() + " can not be empty");
    }

    if (field.getValue().indexOf(' ') !=  -1) {
      notification.addError(field.getCaption() + " can not contain spaces!");
    }

    if (field.getValue().length() > MAX_LENGTH) {
      notification.addError(field.getCaption() + " is too long! Max length : " + MAX_LENGTH);
    }
  }
}
