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

package nl.kpmg.lcm.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import nl.kpmg.lcm.ui.view.AdministrationViewImpl;
import nl.kpmg.lcm.ui.view.MetadataOverviewViewImpl;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.DiscoveryNavigator;

/**
 *
 */
@Theme("lcm_main_theme")
@Widgetset("nl.kpmg.lcm.ui.LCMWidgetset")
@Scope("prototype")
@Component(value = "ui")
@SpringUI
public class Application extends UI {

  private Navigator navigator;

  @Override
  protected void init(VaadinRequest vaadinRequest) {
    getPage().setTitle("Local Catalog Manager");

    final VerticalLayout root = new VerticalLayout();
    root.setSizeFull();
    root.setMargin(true);
    root.setSpacing(true);
    setContent(root);

    final CssLayout navigationBar = new CssLayout();
    navigationBar.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

    Label logo = new Label("LCM");
    logo.addStyleName("logo");
    logo.setWidthUndefined();

    navigationBar.addComponent(logo);
    navigationBar
        .addComponent(createNavigationButton("Metadata", MetadataOverviewViewImpl.VIEW_NAME));
    navigationBar.addComponent(createNotImplementedButton("Analytics"));
    navigationBar.addComponent(createNotImplementedButton("Discover"));
    navigationBar
        .addComponent(createNavigationButton("Administration", AdministrationViewImpl.VIEW_NAME));
    root.addComponent(navigationBar);

    final Panel viewContainer = new Panel();
    viewContainer.setSizeFull();
    root.addComponent(viewContainer);
    root.setExpandRatio(viewContainer, 1.0f);

    navigator = new DiscoveryNavigator(this, viewContainer);
    navigator.navigateTo("");
  }

  private Button createNavigationButton(String caption, final String viewName) {
    Button button = new Button(caption);
    button.addStyleName(ValoTheme.BUTTON_SMALL);
    button.addClickListener(new ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        getUI().getNavigator().navigateTo(viewName);
      }
    });
    return button;
  }

  private Button createNotImplementedButton(String caption) {
    Button button = new Button(caption);
    button.addStyleName(ValoTheme.BUTTON_SMALL);
    button.addClickListener(new ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        Notification.show("This functionality is not yet implemented.");
      }
    });
    return button;
  }
}
