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
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.data.RemoteLcm;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.LcmBadRequestException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.DynamicDataContainer;
import nl.kpmg.lcm.ui.view.administration.listeners.FileReceiver;

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
  private DynamicDataContainer dataContainer;

  private TextField nameField = new TextField("Name");
  private TextField uniqueIdField = new TextField("LCM id");
  private TextField applicationIdField = new TextField("Application id");
  private TextField applicationKeyField = new TextField("Application key");
  private ComboBox protocolField = new ComboBox("Protocol");
  private TextField addressField = new TextField("Address");
  private TextField portField = new TextField("Port");
  private Button saveButton = new Button("Save");
  private Upload certificateUpload = new Upload();
  private Label urlLabel = new Label();
  private RemoteLcm remoteLcm;
  private final static int MAX_LENGTH = 128;
  
  private FileReceiver fileReceiver;

  public RemoteLcmCreateWindow(RestClientService restClientService,
      DynamicDataContainer dataContainer) {
    super(DEFAULT_TITLE);
    this.restClientService = restClientService;
    this.dataContainer = dataContainer;
    this.fileReceiver = new FileReceiver(this, restClientService, false);
    init();
  }

  public RemoteLcmCreateWindow(RestClientService restClientService, RemoteLcm lcm,
      DynamicDataContainer dataContainer) throws JsonProcessingException {
    super(EDIT_TITLE);
    this.restClientService = restClientService;
    this.dataContainer = dataContainer;
    this.fileReceiver = new FileReceiver(this, restClientService, true);
    init();
    nameField.setValue(lcm.getName());
    uniqueIdField.setValue(lcm.getUniqueId());
    applicationIdField.setValue(lcm.getApplicationId());
    // Application key is never displayed in security reasons
    applicationKeyField.setValue("");
    protocolField.setValue(lcm.getProtocol());
    addressField.setValue(lcm.getDomain());
    portField.setValue(lcm.getPort().toString());
    remoteLcm = lcm;

    updateURLLabel(0, null);
  }

  private void init() {
    saveButton.addClickListener(this);
    urlLabel.setCaption("URL");
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

    VerticalLayout panelContent = new VerticalLayout();
    panelContent.setMargin(true);

    HorizontalLayout nameLayout = createLayoutForField(nameField);
    panelContent.addComponent(nameLayout);

    HorizontalLayout uniqeIdLayout = createLayoutForField(uniqueIdField);
    panelContent.addComponent(uniqeIdLayout);

    HorizontalLayout applicationIdLayout = createLayoutForField(applicationIdField);
    panelContent.addComponent(applicationIdLayout);

    HorizontalLayout applicationKeyLayout = createLayoutForField(applicationKeyField);
    panelContent.addComponent(applicationKeyLayout);

    HorizontalLayout protocolFieldLayout = createLayoutForField(protocolField);
    panelContent.addComponent(protocolFieldLayout);

    HorizontalLayout addressFieldLayout = createLayoutForField(addressField);
    panelContent.addComponent(addressFieldLayout);

    HorizontalLayout portFieldLayout = createLayoutForField(portField);
    panelContent.addComponent(portFieldLayout);

    HorizontalLayout urlLabelLayout = createLayoutForLabel(urlLabel);
    panelContent.addComponent(urlLabelLayout);

    HorizontalLayout uploadFieldLayout = createUploadLayout();
    panelContent.addComponent(uploadFieldLayout);

    saveButton.addStyleName("margin-top-10");
    panelContent.addComponent(saveButton);

    this.setWidth(PANEL_SIZE);
    this.setModal(true);

    this.setContent(panelContent);
  }

  private HorizontalLayout createUploadLayout() {
    HorizontalLayout fieldLayout = new HorizontalLayout();
    // Create the upload with a caption and set fileReceiver later
    certificateUpload = new Upload("Select certificate file", fileReceiver);
    certificateUpload.addSucceededListener(fileReceiver);
    certificateUpload.addFailedListener(fileReceiver);
    certificateUpload.setButtonCaption(null);

    fieldLayout.setWidth("100%");
    fieldLayout.addStyleName("margin-top-10");
    fieldLayout.addComponent(certificateUpload);

    return fieldLayout;
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
      nl.kpmg.lcm.common.validation.Notification notification =
          new nl.kpmg.lcm.common.validation.Notification();

      validate(notification);
      ObjectMapper mapper = new ObjectMapper();

      if (notification.hasErrors()) {
        Notification.show("Validation failed: " + notification.errorMessage());
        LOGGER.debug("Validation failed: " + notification.errorMessage());
        return;
      }

      ObjectNode rootNode = mapper.createObjectNode();
      rootNode.put("name", nameField.getValue());
      rootNode.put("uniqueId", uniqueIdField.getValue());
      rootNode.put("applicationId", applicationIdField.getValue());
      rootNode.put("applicationKey", applicationKeyField.getValue());
      rootNode.put("protocol", protocolField.getValue().toString());
      rootNode.put("domain", addressField.getValue());
      rootNode.put("port", portField.getValue());
      fileReceiver.setAlias(uniqueIdField.getValue());
      try {
        if (remoteLcm != null) {
          rootNode.put("id", remoteLcm.getId());
          rootNode.put("status", remoteLcm.getStatus());
          restClientService.updateRemoteLcm(rootNode.toString());
        } else {
          restClientService.createRemoteLcm(rootNode.toString());
        }

        certificateUpload.setImmediate(true);
        certificateUpload.submitUpload();
        dataContainer.updateContent();
      } catch (ServerException | IOException ex) {
        Notification.show("Operation of failed!");
        LOGGER.warn("Creation/Update of remote LCM failed.", ex);
      } catch ( LcmBadRequestException | AuthenticationException  ex) {
        Notification.show("Operation of failed! Message: " +  ex.getMessage());
        LOGGER.warn("Creation/Update of remote LCM failed.", ex);
      }
    }
  }

  private void validate(nl.kpmg.lcm.common.validation.Notification notification) {
    validateField(nameField, notification);
    validateField(applicationIdField, notification);
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

  private void validateField(TextField field,
      nl.kpmg.lcm.common.validation.Notification notification) {
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

  private HorizontalLayout createLayoutForField(AbstractField field) {
    return createLayoutForField(field, true);
  }

  private HorizontalLayout createLayoutForField(AbstractField field, boolean isRequired) {
    HorizontalLayout fieldLayout = new HorizontalLayout();
    field.setRequired(isRequired);
    field.setWidth("70%");
    fieldLayout.setWidth("100%");
    fieldLayout.addStyleName("margin-top-10");
    fieldLayout.addComponent(field);

    return fieldLayout;
  }

  private HorizontalLayout createLayoutForLabel(Label label) {
    HorizontalLayout labelLayout = new HorizontalLayout();
    label.setWidth("70%");
    labelLayout.setWidth("100%");
    labelLayout.addStyleName("margin-top-10");
    labelLayout.addComponent(label);

    return labelLayout;
  }
}
