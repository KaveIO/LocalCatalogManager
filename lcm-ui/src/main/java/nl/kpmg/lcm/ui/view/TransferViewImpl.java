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

package nl.kpmg.lcm.ui.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

import nl.kpmg.lcm.rest.types.RemoteLcmsRepresentation;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.discovery.DiscoveryPanel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/****
 *
 * @author mhoekstra
 */
@Component
@SpringView(name = TransferViewImpl.VIEW_NAME)
public class TransferViewImpl extends VerticalLayout implements View {

  /**
   * The linkable name of this view.
   */
  public static final String VIEW_NAME = "transfer";

  /**
   * The service for interacting with the backend.
   */
  @Autowired
  private RestClientService restClientService;

  private RemoteLcmsRepresentation remoteLcms;

  private DiscoveryPanel discoveryPanel;

  /**
   * Builds the interface.
   */
  @PostConstruct
  public final void init() {
    HorizontalLayout root = new HorizontalLayout();
    root.setWidth("100%");
    root.setMargin(true);
    root.setSpacing(true);

    discoveryPanel = new DiscoveryPanel(restClientService);

    root.addComponent(discoveryPanel);

    addComponent(root);
  }

  /**
   * Loads the data on presentation.
   *
   * @param event fired when the view is entered.
   */
  @Override
  public final void enter(final ViewChangeListener.ViewChangeEvent event) {

  }
}
