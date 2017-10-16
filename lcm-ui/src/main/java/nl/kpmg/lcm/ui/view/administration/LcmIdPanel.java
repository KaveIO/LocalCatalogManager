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

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

import nl.kpmg.lcm.common.rest.types.LcmIdRepresentation;

import java.awt.Toolkit;
import java.awt.datatransfer.*;

/**
 *
 * @author shristov
 */
public class LcmIdPanel extends CustomComponent {
  private LcmIdRepresentation lcmId;
  private Panel panel;

  public LcmIdPanel() {
    panel = new Panel();
    setCompositionRoot(panel);
  }

  public void refreshLcmIdPanel() {
    HorizontalLayout root = new HorizontalLayout();
    root.setMargin(true);
    root.setSpacing(true);

    panel.setContent(root);

    root.addComponent(new Label(lcmId.getItem().getLcmId()));
    root.addComponent(new Button("Copy", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        StringSelection stringSelection = new StringSelection(lcmId.getItem().getLcmId());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
      }
    }));
  }

  public void setLcmId(LcmIdRepresentation lcmId) {
    this.lcmId = lcmId;
    refreshLcmIdPanel();
  }
}
