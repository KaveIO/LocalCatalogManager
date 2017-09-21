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

import com.vaadin.ui.Button;
import com.vaadin.ui.UI;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.common.client.ClientException;
import nl.kpmg.lcm.common.data.RemoteLcm;
import nl.kpmg.lcm.common.rest.types.RemoteLcmRepresentation;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.DataCreationException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.DynamicDataContainer;

import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author shristov
 */
public class ImportUsersFromRemoteLcmListener extends AbstractListener {
  private static final org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(ImportUsersFromRemoteLcmListener.class.getName());

  /**
   * @param dataContainer parent view.
   */
  public ImportUsersFromRemoteLcmListener(final DynamicDataContainer dataContainer,
      final RestClientService restClientService) {
    super(dataContainer, restClientService);
  }

  @Override
  public void buttonClick(final Button.ClickEvent event) {
    RemoteLcmRepresentation data = (RemoteLcmRepresentation) event.getButton().getData();
    RemoteLcm item = data.getItem();
    String message =
        String.format("Are you sure that you want to import users from %s ?", item.getName());
    ConfirmDialog.show(UI.getCurrent(), "Warning", message, "Yes", "No",
        new ConfirmDialog.Listener() {
          public void onClose(ConfirmDialog dialog) {
            if (dialog.isConfirmed()) {
              try {
                importUsers(item.getId());
                dataContainer.updateContent();
              } catch (Exception e) {
                LOGGER.error(String.format("Unable to import users from %s . Error message: %s",
                    item.getName(), e.getMessage()));
                String notMessage =
                    String.format("Unable to import users from %s !",
                        item.getName());
                com.vaadin.ui.Notification.show(notMessage);
              }

            }
          }
        });
  }

  protected String getItemName() {
    return "remote LCM";
  }

  protected void importUsers(String id) throws AuthenticationException, ServerException,
      ClientException, DataCreationException {
    restClientService.importUsersFromRemoteLcm(id);
  }
}
