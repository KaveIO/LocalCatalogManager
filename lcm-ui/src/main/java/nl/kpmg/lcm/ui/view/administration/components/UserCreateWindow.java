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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component.Listener;
import com.vaadin.ui.Field.ValueChangeEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import nl.kpmg.lcm.common.Roles;
import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.LcmBadRequestException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.DynamicDataContainer;

import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 *
 * @author mhoekstra
 */
public class UserCreateWindow extends Window implements Button.ClickListener, Listener {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UserCreateWindow.class
      .getName());
  /**
   * The default size of the side panels of this view.
   */
  private static final String PANEL_SIZE = "600px";

  /**
   * The default size of the side panels of this view.
   */
  private static final String CREATE_TITLE = "Create User";
  private static final String EDIT_TITLE = "Edit User";

  private RestClientService restClientService;
  private DynamicDataContainer dataContainer;
  private final TextField nameField = new TextField("Name");
  private ComboBox rolesListComboBox = new ComboBox("Role");
  private final TextField originField = new TextField("Origin");
  private final TextField pField = new TextField("Password");
  private final TextArea pathListArea = new TextArea("Accessible paths");
  private final TextArea metadataListArea = new TextArea("Accessible metadatas");
  private final Button saveButton = new Button("Save");
  private User user;
  private final static int MAX_LENGTH = 128;
  private final static int MIN_PASSWORD_LENGTH = 5;

  private final String LIST_DELIMITER = ";";
  private boolean isCreateOpereration;

  public UserCreateWindow(RestClientService restClientService, DynamicDataContainer dataContainer) {
    super(CREATE_TITLE);
    isCreateOpereration = true;
    this.restClientService = restClientService;
    this.dataContainer = dataContainer;
    init();
    pathListArea.setDescription("Each path must be terminated by: " + LIST_DELIMITER);
    metadataListArea.setDescription("Each metadata Id must be terminated by: " + LIST_DELIMITER);
  }

  public UserCreateWindow(RestClientService restClientService, User user,
      DynamicDataContainer dataContainer) throws JsonProcessingException {
    super(EDIT_TITLE);
    isCreateOpereration = false;
    this.restClientService = restClientService;
    this.dataContainer = dataContainer;
    init();
    nameField.setValue(user.getName());
    nameField.setEnabled(false);
    rolesListComboBox.setValue(user.getRole());
    initRoleFields(user.getRole());

    if(user.getRole().equals(Roles.REMOTE_USER)) {
        rolesListComboBox.setEnabled(false);
        pField.setEnabled(false);
        originField.setValue(user.getOrigin());
    }
    originField.setEnabled(false);

    StringBuilder pathList = new StringBuilder();
    if (user.getAllowedPathList() != null && user.getAllowedPathList().size() > 0) {
      for (String path : user.getAllowedPathList()) {
        pathList.append(path + LIST_DELIMITER);
      }
    }

    if (pathList.length() > 0) {
      pathListArea.setValue(pathList.toString());
    }

    StringBuilder metadataList = new StringBuilder();
    if (user.getAllowedMetadataList() != null && user.getAllowedMetadataList().size() > 0) {
      for (String metadataId : user.getAllowedMetadataList()) {
        metadataList.append(metadataId + LIST_DELIMITER);
      }
    }

    if (metadataList.length() > 0) {
      metadataListArea.setValue(metadataList.toString());
    }

    this.user = user;
  }

  private void init() {
    saveButton.addClickListener(this);

    FormLayout panelContent = new FormLayout();
    panelContent.setMargin(true);
    nameField.setRequired(true);
    rolesListComboBox = initRolesListComboBox();
    pField.setId("userp");
    pathListArea.setWidth("100%");
    pathListArea.setHeight("100%");
    metadataListArea.setWidth("100%");
    metadataListArea.setHeight("100%");

    panelContent.addComponent(nameField);
    panelContent.addComponent(rolesListComboBox);
    panelContent.addComponent(pField);
    panelContent.addComponent(originField);
    panelContent.addComponent(pathListArea);
    panelContent.addComponent(metadataListArea);
    panelContent.addComponent(saveButton);

    this.setWidth(PANEL_SIZE);
    this.setModal(true);

    this.setContent(panelContent);
  }

  private ComboBox initRolesListComboBox() {
    ComboBox rolesListComboBox = new ComboBox("Role");

    rolesListComboBox.addItem(Roles.ADMINISTRATOR);
    rolesListComboBox.setItemCaption(Roles.ADMINISTRATOR, "Administrator");

    rolesListComboBox.addItem(Roles.REMOTE_USER);
    rolesListComboBox.setItemCaption(Roles.REMOTE_USER, "Remote user");

    rolesListComboBox.addItem(Roles.API_USER);
    rolesListComboBox.setItemCaption(Roles.API_USER, "API user");

    rolesListComboBox.setTextInputAllowed(false);
    rolesListComboBox.setRequired(true);
    rolesListComboBox.setNullSelectionAllowed(false);
    rolesListComboBox.setInputPrompt("Please select one");
    rolesListComboBox.addListener(this);

    return rolesListComboBox;
  }

  public void componentEvent(Event event) {
    if (!(event instanceof ValueChangeEvent)) {
      return;// skip all other events
    }

    ValueChangeEvent vcEvent = (ValueChangeEvent) event;
    String role = (String) vcEvent.getProperty().getValue();
    initRoleFields(role);
  }

    private void initRoleFields(String role) throws Property.ReadOnlyException {
        switch (role) {
            case Roles.REMOTE_USER:
                pField.setEnabled(false);
                pField.setRequired(false);
                pField.setValue("");
                originField.setEnabled(true);
                originField.setRequired(true);
                break;
            case Roles.ADMINISTRATOR:
            case Roles.API_USER:
                pField.setEnabled(true);
                pField.setRequired(true);
                originField.setEnabled(false);
                originField.setRequired(false);
                originField.setValue("");
                break;
            default:
                pField.setEnabled(true);
                pField.setRequired(true);
                originField.setEnabled(true);
                originField.setRequired(false);
                originField.setValue("");
        }
    }

  @Override
  public void buttonClick(Button.ClickEvent event) {
    if (event.getSource() == saveButton) {
      nl.kpmg.lcm.common.validation.Notification notification =
          new nl.kpmg.lcm.common.validation.Notification();

      validate(notification);

      if (notification.hasErrors()) {
        Notification.show("Validation failed: " + notification.errorMessage());
        LOGGER.debug("Validation failed: " + notification.errorMessage());
        return;
      }

      ObjectNode rootNode = createJsonNode();

      try {
        if (!isCreateOpereration) {
          if(user != null) {
            rootNode.put("id", user.getId());
          }
          restClientService.updateUser(rootNode.toString());
          Notification.show("Update was successful.");
        } else {
          restClientService.createUser(rootNode.toString());
          Notification.show("Creation of user was successful.");
        }
        dataContainer.updateContent();
        this.close();
      } catch (ServerException | IOException ex) {
        Notification.show("Creation of user failed.");
        LOGGER.warn("Creation of user failed.", ex);
      } catch (LcmBadRequestException | AuthenticationException ex) {
        Notification.show("Creation of user failed. Message: " + ex.getMessage());
        LOGGER.warn("Creation of user failed.", ex);
      } 
    }
  }

  private ObjectNode createJsonNode() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootNode = mapper.createObjectNode();
    rootNode.put("name", nameField.getValue());
    String role = (String)rolesListComboBox.getValue();
    rootNode.put("role", role);
    if(!Roles.REMOTE_USER.equals(role)){
        rootNode.put("newPassword", pField.getValue());
    }
    String formOrigin = originField.getValue().length() > 0 ? originField.getValue(): User.LOCAL_ORIGIN;
    String origin = user !=  null ? user.getOrigin() : formOrigin;
    rootNode.put("origin", origin);
    ArrayNode pathArrayNode = mapper.createArrayNode();
    String[] allowedPaths = pathListArea.getValue().split(LIST_DELIMITER);
    for (String path : allowedPaths) {
      if (path.length() > 0) {
        pathArrayNode.add(path);
      }
    }

    if (allowedPaths.length > 0) {
      rootNode.set("allowedPathList", pathArrayNode);
    }
    ArrayNode metadataArrayNode = mapper.createArrayNode();
    String[] allowedMetadatas = metadataListArea.getValue().split(LIST_DELIMITER);
    for (String metadataId : allowedMetadatas) {
      if (metadataId.length() > 0) {
        metadataArrayNode.add(metadataId);
      }
    }

    if (allowedMetadatas.length > 0) {
      rootNode.set("allowedMetadataList", metadataArrayNode);
    }
    if (user != null) {
      rootNode.put("id", user.getId());
    }
    return rootNode;
  }

  private void validate(nl.kpmg.lcm.common.validation.Notification notification) {
    validateText(nameField, notification);
    validateComboBox(rolesListComboBox, notification);
    String role =  (String)rolesListComboBox.getValue();
    if (isCreateOpereration && !role.equals(Roles.REMOTE_USER)
            && pField.getValue().isEmpty()) {
      notification.addError(pField.getCaption() + " can not be empty");
    }
    if (pField.getValue().length() > 0 && pField.getValue().length() < MIN_PASSWORD_LENGTH
            ) {
      notification.addError(pField.getCaption() + " can not less then "
          + MIN_PASSWORD_LENGTH + " symbols");
    }

    if (pField.getValue().length() > MAX_LENGTH) {
      notification
          .addError(pField.getCaption() + " is too long! Max length : " + MAX_LENGTH);
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

  private void validateComboBox(ComboBox comboBox,
      nl.kpmg.lcm.common.validation.Notification notification) {
    if (comboBox.getValue() == null) {
      notification.addError(comboBox.getCaption() + " can not be null!");
    }
  }
}
