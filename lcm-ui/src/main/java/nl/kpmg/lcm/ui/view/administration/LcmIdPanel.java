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
package nl.kpmg.lcm.ui.view.administration;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import nl.kpmg.lcm.common.rest.types.LcmIdRepresentation;

import org.slf4j.LoggerFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.io.File;

/**
 *
 * @author shristov
 */
public class LcmIdPanel extends CustomComponent {
  private LcmIdRepresentation lcmId;
  private Panel panel;
  private String certificateFilename = "server.cer";
  private String certificateFilepath;
  
  
  private static final org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(LcmIdPanel.class.getName());

  public LcmIdPanel(String certificateFilepath) {
    panel = new Panel();
    this.certificateFilepath = certificateFilepath;
    setCompositionRoot(panel);
  }

  public void refreshLcmIdPanel() {
    VerticalLayout panelContent = new VerticalLayout();
    panelContent.setMargin(true);
    panelContent.setSpacing(true);

    HorizontalLayout idLayout = new HorizontalLayout();
    idLayout.setMargin(true);
    idLayout.setSpacing(true);
    idLayout.addComponent(new Label("Your LCM id: " + lcmId.getItem().getLcmId()));
    idLayout.addComponent(new Button("Copy", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        StringSelection stringSelection = new StringSelection(lcmId.getItem().getLcmId());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
      }
    }));

    HorizontalLayout certificateLayout = new HorizontalLayout();
    certificateLayout.setMargin(true);
    certificateLayout.setSpacing(true);
    certificateLayout.addComponent(new Label(
        "Certificate file that need to be given to the Authorized LCM. "));
    FileResource certificateResource = new FileResource(new File(certificateFilepath));
    FileDownloader fileDownloader = new FileDownloader(certificateResource);
    Button certificateDownload = new Button("Download Certificate");
    fileDownloader.extend(certificateDownload);

    certificateLayout.addComponent(certificateDownload);

    panelContent.addComponent(idLayout);
    panelContent.addComponent(certificateLayout);
    panel.setContent(panelContent);
  }

  public void setLcmId(LcmIdRepresentation lcmId) {
    this.lcmId = lcmId;
    refreshLcmIdPanel();
  }

}