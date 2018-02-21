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
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.data.UserGroup;
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
public class UserGroupCreateWindow extends Window implements Button.ClickListener {

  private static final org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(UserGroupCreateWindow.class.getName());
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
  private final TextArea userListArea = new TextArea("Users");
  private final TextArea pathListArea = new TextArea("Accessible paths");
  private final TextArea metadataListArea = new TextArea("Accessible metadatas");
  private final Button saveButton = new Button("Save");
  private UserGroup userGroup;
  private final static int MAX_LENGTH = 128;
  private final static int MIN_PASSWORD_LENGTH = 5;

  private final String LIST_DELIMITER = ";";
  private boolean isCreateOpereration;

  public UserGroupCreateWindow(RestClientService restClientService,
      DynamicDataContainer dataContainer) {
    super(CREATE_TITLE);
    isCreateOpereration = true;
    this.restClientService = restClientService;
    this.dataContainer = dataContainer;
    init();
    userListArea.setDescription("Each user that is part of the group terminated by: "
        + LIST_DELIMITER);
    pathListArea.setDescription("Each path must be terminated by: " + LIST_DELIMITER);
    metadataListArea.setDescription("Each metadata Id must be terminated by: " + LIST_DELIMITER);
  }

  public UserGroupCreateWindow(RestClientService restClientService, UserGroup userGroup,
      DynamicDataContainer dataContainer)
      throws JsonProcessingException {
    super(EDIT_TITLE);
    isCreateOpereration = false;
    this.restClientService = restClientService;
    this.dataContainer = dataContainer;
    nameField.setValue(userGroup.getName());
    nameField.setEnabled(false);

    StringBuilder userList = new StringBuilder();
    if (userGroup.getUsers() != null) {
      for (String user : userGroup.getUsers()) {
        userList.append(user + LIST_DELIMITER);
      }
    }

    if (userList.length() > 0) {
      userListArea.setValue(userList.toString());
    }

    StringBuilder pathList = new StringBuilder();
    if (userGroup.getAllowedPathList() != null) {
      for (String path : userGroup.getAllowedPathList()) {
        pathList.append(path + LIST_DELIMITER);
      }
    }

    if (pathList.length() > 0) {
      pathListArea.setValue(pathList.toString());
    }

    StringBuilder metadataList = new StringBuilder();
    if (userGroup.getAllowedPathList() != null) {
      for (String metadataId : userGroup.getAllowedMetadataList()) {
        metadataList.append(metadataId + LIST_DELIMITER);
      }
    }

    if (metadataList.length() > 0) {
      metadataListArea.setValue(metadataList.toString());
    }

    this.userGroup = userGroup;
    init();
  }

  private void init() {
    saveButton.addClickListener(this);

    FormLayout panelContent = new FormLayout();
    panelContent.setMargin(true);
    nameField.setRequired(true);
    userListArea.setWidth("100%");
    userListArea.setHeight("100%");
    pathListArea.setWidth("100%");
    pathListArea.setHeight("100%");
    metadataListArea.setWidth("100%");
    metadataListArea.setHeight("100%");

    panelContent.addComponent(nameField);
    panelContent.addComponent(userListArea);
    panelContent.addComponent(pathListArea);
    panelContent.addComponent(metadataListArea);
    panelContent.addComponent(saveButton);

    this.setWidth(PANEL_SIZE);
    this.setModal(true);

    this.setContent(panelContent);
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
          if (userGroup != null) {
            rootNode.put("id", userGroup.getId());
          }
          restClientService.updateUserGroup(rootNode.toString());
          Notification.show("Update was successful.");
        } else {
          restClientService.createUserGroup(rootNode.toString());
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

    ArrayNode userArrayNode = mapper.createArrayNode();
    String[] users = userListArea.getValue().split(LIST_DELIMITER);
    for (String path : users) {
      if (path.length() > 0) {
        userArrayNode.add(path);
      }
    }
    if (users.length > 0) {
      rootNode.set("users", userArrayNode);
    }

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
    if (userGroup != null) {
      rootNode.put("id", userGroup.getId());
    }
    return rootNode;
  }

  private void validate(nl.kpmg.lcm.common.validation.Notification notification) {
    validateText(nameField, notification);
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
