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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.data.RemoteLcm;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.DataCreationException;
import nl.kpmg.lcm.ui.rest.RestClientService;

import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 *
 * @author mhoekstra
 */
public class RemoteLcmCreateWindow extends Window implements Button.ClickListener {

  private static final org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(RemoteLcmCreateWindow.class.getName());
  /**
   * The default size of the side panels of this view.
   */
  private static final String PANEL_SIZE = "600px";

  /**
   * The default size of the side panels of this view.
   */
  private static final String DEFAULT_TITLE = "Create Remote LCM";
  private static final String EDIT_TITLE = "Edit Remote LCM";

  private RestClientService restClientService;

  private TextField nameField = new TextField("Name");
  private ComboBox protocolField = new ComboBox("Protocol");
  private TextField addressField = new TextField("Address");
  private TextField portField = new TextField("Port");
  private Button saveButton = new Button("Save");
  private Label urlLabel = new Label();
  private RemoteLcm remoteLcm;
  private final static int MAX_LENGTH = 128;

  public RemoteLcmCreateWindow(RestClientService restClientService) {
    super(DEFAULT_TITLE);
    this.restClientService = restClientService;
    init();
  }

  public RemoteLcmCreateWindow(RestClientService restClientService, RemoteLcm lcm)
      throws JsonProcessingException {
    super(EDIT_TITLE);
    this.restClientService = restClientService;
    init();
    nameField.setValue(lcm.getName());
    protocolField.setValue(lcm.getProtocol());
    addressField.setValue(lcm.getDomain());
    portField.setValue(lcm.getPort().toString());
    remoteLcm = lcm;
    updateURLLabel(0, null);
  }

  private void init() {
    saveButton.addClickListener(this);
    urlLabel.setCaption("URL");
    FormLayout panelContent = new FormLayout();
    panelContent.setMargin(true);
    protocolField.setInputPrompt("Select protocol");
    protocolField.addItem("http");
    protocolField.addItem("https");
    protocolField.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        updateURLLabel(1, (String) event.getProperty().getValue());
      }
    });
    addressField.addTextChangeListener(new URLUpdater(2));
    portField.addTextChangeListener(new URLUpdater(3));

    panelContent.addComponent(nameField);
    panelContent.addComponent(protocolField);
    panelContent.addComponent(addressField);
    panelContent.addComponent(portField);
    panelContent.addComponent(urlLabel);
    panelContent.addComponent(saveButton);

    this.setWidth(PANEL_SIZE);
    this.setModal(true);

    this.setContent(panelContent);
  }

  private void updateURLLabel(int editedTextIndex, String currentText) {
    if (currentText == null) {
      currentText = "";
    }

    String protocolFieldValue = "";
    if (protocolField.getValue() != null) {
      protocolFieldValue = protocolField.getValue().toString();
    }

    String protocol = editedTextIndex == 1 ? currentText : protocolFieldValue;
    String address = editedTextIndex == 2 ? currentText : addressField.getValue();
    String port = editedTextIndex == 3 ? currentText : portField.getValue();

    String template = "%s://%s:%s";
    String url = String.format(template, protocol, address, port);

    urlLabel.setValue(url);
  }

  @Override
  public void buttonClick(Button.ClickEvent event) {
    if (event.getSource() == saveButton) {
      nl.kpmg.lcm.common.validation.Notification notification = new nl.kpmg.lcm.common.validation.Notification();

      validate(notification);
      ObjectMapper mapper = new ObjectMapper();

      if (notification.hasErrors()) {
        Notification.show("Validation failed: " + notification.errorMessage());
        LOGGER.debug("Validation failed: " + notification.errorMessage());
        return;
      }

      ObjectNode rootNode = mapper.createObjectNode();
      rootNode.put("name", nameField.getValue());
      rootNode.put("protocol", protocolField.getValue().toString());
      rootNode.put("domain", addressField.getValue());
      rootNode.put("port", portField.getValue());
      try {
        if (remoteLcm != null) {
          rootNode.put("id", remoteLcm.getId());
          restClientService.updateRemoteLcm(rootNode.toString());
          Notification.show("Update finished successfully.");
        } else {
          restClientService.createRemoteLcm(rootNode.toString());
          Notification.show("Creation finished successfully.");
        }
        this.close();
      } catch (ServerException | DataCreationException | AuthenticationException | IOException ex) {
        Notification.show("Creation of failed!");
        LOGGER.warn("Creation of remote LCM failed.", ex);
      }
    }
  }

  private void validate(nl.kpmg.lcm.common.validation.Notification notification) {
    validateField(nameField, notification);
    validateField(addressField, notification);
    validateField(portField, notification);
    if (protocolField.isEmpty()) {
      notification.addError(protocolField.getCaption() + " can not be empty");
    }

    try {
      Integer.parseInt(portField.getValue());
    } catch (NumberFormatException nfe) {
      notification.addError(portField.getCaption() + " should be valid integer!");
    }
  }

  private void validateField(TextField field, nl.kpmg.lcm.common.validation.Notification notification) {
    if (field.getValue().isEmpty()) {
      notification.addError(field.getCaption() + " can not be empty");
    }

    if (field.getValue().length() > MAX_LENGTH) {
      notification.addError(field.getCaption() + " is too long! Max length : " + MAX_LENGTH);
    }
  }

  class URLUpdater implements TextChangeListener {
    private int index;

    URLUpdater(int index) {
      this.index = index;
    }

    @Override
    public void textChange(TextChangeEvent event) {
      updateURLLabel(index, event.getText());
    }
  }
}
