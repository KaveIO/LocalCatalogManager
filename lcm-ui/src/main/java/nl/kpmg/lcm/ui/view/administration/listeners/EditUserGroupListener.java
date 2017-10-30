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
package nl.kpmg.lcm.ui.view.administration.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;

import nl.kpmg.lcm.common.data.UserGroup;
import nl.kpmg.lcm.common.rest.types.UserGroupRepresentation;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.DynamicDataContainer;
import nl.kpmg.lcm.ui.view.administration.components.UserGroupCreateWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shristov
 */

/**
 * Edit listener for changing the storage object.
 */
public class EditUserGroupListener extends AbstractListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(EditUserGroupListener.class.getName());

  /**
   * @param dataContainer parent view.
   */
  public EditUserGroupListener(final DynamicDataContainer dataContainer,
      final RestClientService restClientService) {
     super(dataContainer, restClientService);
  }

  @Override
  public void buttonClick(final Button.ClickEvent event) {

    UserGroupRepresentation data = (UserGroupRepresentation) event.getButton().getData();
    UserGroup userGroup = data.getItem();
    try {
      UserGroupCreateWindow userGroupCreateWindow =
          new UserGroupCreateWindow(restClientService, userGroup, dataContainer);
      UI.getCurrent().addWindow(userGroupCreateWindow);

    } catch (JsonProcessingException ex) {
      LOGGER.error("Unable to parse user. Error message: " + ex.getMessage());
      com.vaadin.ui.Notification.show("Update failed: " + ex.getMessage());
    }
  }
}
