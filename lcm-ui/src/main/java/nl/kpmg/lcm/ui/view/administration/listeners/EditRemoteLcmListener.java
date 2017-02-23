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
import com.vaadin.ui.Window;

import nl.kpmg.lcm.rest.types.RemoteLcmRepresentation;
import nl.kpmg.lcm.server.data.RemoteLcm;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.DynamicDataContainer;
import nl.kpmg.lcm.ui.view.administration.components.RemoteLcmCreateWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shristov
 */

/**
 * Edit listener for changing the storage object.
 */
public class EditRemoteLcmListener extends AbstractListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(EditRemoteLcmListener.class
      .getName());

  public EditRemoteLcmListener(final DynamicDataContainer dataContainer,
      RestClientService restClientService) {
    super(dataContainer, restClientService);
  }

  @Override
  public void buttonClick(final Button.ClickEvent event) {

    RemoteLcmRepresentation data = (RemoteLcmRepresentation) event.getButton().getData();
    RemoteLcm lcm = data.getItem();
    try {
      RemoteLcmCreateWindow remoteLcmCreateWindow =
          new RemoteLcmCreateWindow(restClientService, lcm);
      remoteLcmCreateWindow.addCloseListener(new Window.CloseListener() {
        @Override
        public void windowClose(Window.CloseEvent e) {
          dataContainer.updateContent();
        }
      });
      UI.getCurrent().addWindow(remoteLcmCreateWindow);

    } catch (JsonProcessingException ex) {
      LOGGER.error("Unable to parse storage. Error message: " + ex.getMessage());
      com.vaadin.ui.Notification.show("Update failed: " + ex.getMessage());
    }
  }
}
