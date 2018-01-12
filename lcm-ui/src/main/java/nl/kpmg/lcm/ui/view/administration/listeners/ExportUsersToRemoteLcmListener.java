/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.kpmg.lcm.ui.view.administration.listeners;

import com.vaadin.ui.Button;
import com.vaadin.ui.UI;

import nl.kpmg.lcm.common.data.RemoteLcm;
import nl.kpmg.lcm.common.rest.types.RemoteLcmRepresentation;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.DynamicDataContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author shristov
 */
public class ExportUsersToRemoteLcmListener extends AbstractListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExportUsersToRemoteLcmListener.class
      .getName());

  public ExportUsersToRemoteLcmListener(final DynamicDataContainer dataContainer,
      final RestClientService restClientService) {
    super(dataContainer, restClientService);
  }


  @Override
  public void buttonClick(Button.ClickEvent event) {
    RemoteLcmRepresentation data = (RemoteLcmRepresentation) event.getButton().getData();
    RemoteLcm item = data.getItem();
    String message =
        String.format("Are you sure that you want to export users to %s ?", item.getName());
    ConfirmDialog.show(UI.getCurrent(), "Warning", message, "Yes", "No",
        new ConfirmDialog.Listener() {
          public void onClose(ConfirmDialog dialog) {
            if (dialog.isConfirmed()) {
              try {
                String resultMessage = exportUsers(item.getId());
                dataContainer.updateContent();
                com.vaadin.ui.Notification.show("The export completed. Result: " + resultMessage);
              } catch (Exception e) {
                LOGGER.error(String.format("Unable to export users to %s . Error message: %s",
                    item.getName(), e.getMessage()));
                String notMessage = String.format("Unable to export users to %s !", item.getName());
                com.vaadin.ui.Notification.show(notMessage);
              }

            }
          }
        });
  }

  protected String getItemName() {
    return "remote LCM";
  }

  protected String exportUsers(String id) {
    return restClientService.exportUsersToRemoteLcm(id);
  }
}
