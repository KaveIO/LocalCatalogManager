/*
 * Copyright 2016 KPMG N.V. (unless otherwise stated).
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

package nl.kpmg.lcm.ui.view.metadata;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;

import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.DataCreationException;
import nl.kpmg.lcm.ui.rest.RestClientService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mhoekstra
 */
public class MetadataCreateWindow extends Window implements Button.ClickListener {

  /**
   * The default size of the side panels of this view.
   */
  private static final String PANEL_SIZE = "600px";

  /**
   * The default size of the side panels of this view.
   */
  private static final String DEFAULT_TITLE = "Create Metadata";

  private RestClientService restClientService;

  private TextArea textArea;

  private Button saveButton;

  public MetadataCreateWindow(RestClientService restClientService) {
    super(DEFAULT_TITLE);
    this.restClientService = restClientService;
    init();
  }

  private void init() {
    textArea = new TextArea();
    textArea.setWidth("100%");
    textArea.setHeight("100%");

    saveButton = new Button("Save");
    saveButton.addClickListener(this);

    FormLayout panelContent = new FormLayout();
    panelContent.setMargin(true);
    panelContent.addComponent(textArea);
    panelContent.addComponent(saveButton);

    this.setWidth(PANEL_SIZE);
    this.setModal(true);

    this.setContent(panelContent);
  }

  @Override
  public void buttonClick(Button.ClickEvent event) {
    if (event.getSource() == saveButton) {
      try {
        String metadata = textArea.getValue();
        restClientService.postMetadata(metadata);
        Notification.show("Creation of metadata successful.");
        this.close();
      } catch (ServerException | DataCreationException | AuthenticationException ex) {
        Notification.show("Creation of metadata failed.");
        Logger.getLogger(MetadataCreateWindow.class.getName()).log(Level.WARNING,
            "Creation of metadata failed.", ex);
      }
    }
  }
}
