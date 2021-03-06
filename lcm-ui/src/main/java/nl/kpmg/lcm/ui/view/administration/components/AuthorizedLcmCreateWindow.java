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
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import nl.kpmg.lcm.common.RandomString;
import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.data.AuthorizedLcm;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.common.rest.authentication.PasswordHash;
import nl.kpmg.lcm.common.rest.authentication.UserPasswordHashException;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.LcmBadRequestException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.DynamicDataContainer;

import org.slf4j.LoggerFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

/**
 *
 * @author mhoekstra
 */
public class AuthorizedLcmCreateWindow extends Window implements Button.ClickListener {

  private static final org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(AuthorizedLcmCreateWindow.class.getName());
  /**
   * The default size of the side panels of this view.
   */
  private static final String PANEL_SIZE = "600px";

  /**
   * The default size of the side panels of this view.
   */
  private static final String CREATE_TITLE = "Add authorized LCM";
  private static final String EDIT_TITLE = "Edit authorized LCM";

  private RestClientService restClientService;
  private DynamicDataContainer dataContainer;

  private final TextField nameField = new TextField("Name");
  private final TextField uniqeLcmIdField = new TextField("Lcm id");
  private final TextField applicationIdField = new TextField("Application id");
  private final TextField applicationKeyField = new TextField("Application key");
  private final CheckBox allowImportOfUsersCheckBox = new CheckBox("Allow the import of users from this LCM.");
  private final Button saveButton = new Button("Save");
  private final Button copyButton = new Button("Copy");
  private final Button generateButton = new Button("Regenerate");
  private AuthorizedLcm authorizedLcm;
  private final static int MAX_LENGTH = 128;
  private final Label createWarning = new Label("Note: Remember the key! It will be hidden after creation!");
  private final Label editWarning = new Label("Note: The saved key can not be displayed!");

  private boolean isCreateOpereration;
  private String unhashedKey;

  public AuthorizedLcmCreateWindow(RestClientService restClientService,
      DynamicDataContainer dataContainer) {
    super(CREATE_TITLE);
    isCreateOpereration = true;
    this.restClientService = restClientService;
    this.dataContainer = dataContainer;
    initLayouts();
  }

  public AuthorizedLcmCreateWindow(RestClientService restClientService,
      AuthorizedLcm authorizedLcm, DynamicDataContainer dataContainer)
      throws JsonProcessingException {
    super(EDIT_TITLE);
    isCreateOpereration = false;
    this.restClientService = restClientService;
    this.dataContainer = dataContainer;
    nameField.setValue(authorizedLcm.getName());
    uniqeLcmIdField.setValue(authorizedLcm.getUniqueId());
    applicationIdField.setValue(authorizedLcm.getApplicationId());
    // Application key is never displayed in security reasons
    applicationKeyField.setValue("");
    allowImportOfUsersCheckBox.setValue(authorizedLcm.isImportOfUsersAllowed());

    this.authorizedLcm = authorizedLcm;
    initLayouts();
  }

  private void initLayouts() {
    saveButton.addClickListener(this);

    VerticalLayout panelContent = new VerticalLayout();
    panelContent.setMargin(true);

    HorizontalLayout nameLayout = initNameLayout();
    panelContent.addComponent(nameLayout);

    HorizontalLayout uniqeLcmIdLayout = initLcmIdLayout();
    panelContent.addComponent(uniqeLcmIdLayout);

    HorizontalLayout applicationIdLayout = initApplicationIdLayout();
    panelContent.addComponent(applicationIdLayout);

    if(isCreateOpereration) {
        createWarning.addStyleName("warning");
        panelContent.addComponent(createWarning);
    } else {
        editWarning.addStyleName("warning");
        panelContent.addComponent(editWarning);
    }

    HorizontalLayout passwordLayout = initApplicationKeyLayout();
    panelContent.addComponent(passwordLayout);

    HorizontalLayout allowImportOfUsersLayout = initAllowImportOfUsersLayout();
    panelContent.addComponent(allowImportOfUsersLayout);

    saveButton.addStyleName("margin-top-20");
    panelContent.addComponent(saveButton);

    this.setWidth(PANEL_SIZE);
    this.setModal(true);

    this.setContent(panelContent);
  }

    private HorizontalLayout initNameLayout() {
        HorizontalLayout nameLayout = new HorizontalLayout();
        nameField.setRequired(true);
        nameField.setWidth("70%");
        nameLayout.setWidth("100%");
        nameLayout.addStyleName("margin-top-10");
        nameLayout.addComponent(nameField);
        return nameLayout;
    }

    private HorizontalLayout initLcmIdLayout() {
        HorizontalLayout uniqeLcmIdLayout = new HorizontalLayout();
        uniqeLcmIdField.setRequired(true);
        uniqeLcmIdField.setWidth("70%");
        uniqeLcmIdLayout.setWidth("100%");
        uniqeLcmIdLayout.addStyleName("margin-top-10");
        uniqeLcmIdLayout.addComponent(uniqeLcmIdField);
        return uniqeLcmIdLayout;
    }

    private HorizontalLayout initApplicationIdLayout() {
        HorizontalLayout applicationIdLayout = new HorizontalLayout();
        applicationIdField.setRequired(true);
        applicationIdField.setWidth("70%");
        applicationIdLayout.setWidth("100%");
        applicationIdLayout.addStyleName("margin-top-10");
        applicationIdLayout.addComponent(applicationIdField);
        return applicationIdLayout;
    }

    private HorizontalLayout initApplicationKeyLayout() throws LcmException, Property.ReadOnlyException {
        HorizontalLayout applicationKeyLayout = new HorizontalLayout();
        applicationKeyField.setRequired(true);
        applicationKeyLayout.addComponent(applicationKeyField);
        if(isCreateOpereration) {
            String hashedKey = generateApplicationKey();
            applicationKeyField.setValue(hashedKey);
            copyButton.addStyleName("generate-button");
            copyButton.setWidth("30%");
            copyButton.addClickListener(this);
            applicationKeyField.setWidth("100%");
            applicationKeyLayout.addComponent(copyButton);
        } else {
            applicationKeyField.setWidth("100%");
            generateButton.addStyleName("generate-button");
            generateButton.setWidth("40%");
            generateButton.addClickListener(this);
            applicationKeyLayout.addComponent(generateButton);
        }

        applicationKeyLayout.setWidth("100%");return applicationKeyLayout;
    }

  private HorizontalLayout initAllowImportOfUsersLayout() {
    HorizontalLayout allowImportOfUsersLayout = new HorizontalLayout();
    allowImportOfUsersCheckBox.setWidth("70%");
    allowImportOfUsersLayout.addStyleName("margin-top-20");
    allowImportOfUsersLayout.addComponent(allowImportOfUsersCheckBox);

    return allowImportOfUsersLayout;
  }

  private String generateApplicationKey() throws LcmException {
    RandomString generator = new RandomString(12, "^_!&@");
        unhashedKey = generator.nextString();
        String hashedKey;
        try {
            hashedKey = PasswordHash.createHash(unhashedKey);
        } catch (UserPasswordHashException ex) {
            LOGGER.error("Unable to create hash! "  +  ex.getMessage());
            Notification.show("Unable to create authorized LCM!");
            throw new LcmException("Unable to create hash!", ex);
        }
        return hashedKey;
    }

  @Override
  public void buttonClick(Button.ClickEvent event) {
    if (event.getSource() == copyButton) {
        StringSelection stringSelection = new StringSelection(applicationKeyField.getValue());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    } else if (event.getSource() == generateButton) {
        String hashedKey = generateApplicationKey();
        applicationKeyField.setValue(hashedKey);
        StringSelection stringSelection = new StringSelection(applicationKeyField.getValue());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    } else if (event.getSource() == saveButton) {
      nl.kpmg.lcm.common.validation.Notification notification =
          new nl.kpmg.lcm.common.validation.Notification();

      validate(notification);
      ObjectMapper mapper = new ObjectMapper();

      ObjectNode rootNode = mapper.createObjectNode();
      rootNode.put("name", nameField.getValue());
      rootNode.put("applicationId", applicationIdField.getValue());
      rootNode.put("applicationKey", unhashedKey);
      rootNode.put("uniqueId", uniqeLcmIdField.getValue());
      rootNode.put("importOfUsersAllowed", allowImportOfUsersCheckBox.getValue());

      try {
        if (authorizedLcm != null) {
          rootNode.put("id", authorizedLcm.getId());
          restClientService.updateAuthorizedLcm(rootNode.toString());
          Notification.show("Update was successful.");
        } else {
          restClientService.createAuthorizedLcm(rootNode.toString());
          Notification.show("Creation is successful.");
        }
        dataContainer.updateContent();
        this.close();
      } catch (ServerException | IOException ex) {
        Notification.show("Operation failed.");
        LOGGER.warn("Creation of authorized LCM failed.", ex);
      } catch (AuthenticationException | LcmBadRequestException  ex) {
        Notification.show("Operation failed. Message " +  ex.getMessage());
        LOGGER.warn("Creation of authorized LCM failed. Message: ", ex);
      }
    }
  }

  private void validate(nl.kpmg.lcm.common.validation.Notification notification) {
    validateText(nameField, notification);
    validateText(uniqeLcmIdField, notification);
    validateText(applicationIdField, notification);
    validateText(applicationKeyField, notification);
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
