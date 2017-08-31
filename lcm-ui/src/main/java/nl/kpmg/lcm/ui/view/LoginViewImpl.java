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
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginListener;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.RestClientService;

import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author mhoekstra
 */
@UIScope
@SpringView(name = LoginViewImpl.VIEW_NAME)
public class LoginViewImpl extends LoginForm implements View, LoginListener {

  public static final String VIEW_NAME = "login";

  @Autowired
  private RestClientService restClientService;

  @Override
  public void enter(ViewChangeListener.ViewChangeEvent event) {
    if (restClientService.isAuthenticated()) {
      getUI().getNavigator().navigateTo(MetadataOverviewViewImpl.VIEW_NAME);
    }
  }



  @Override
  protected com.vaadin.ui.Component createContent(TextField userNameField,
      PasswordField passwordField, Button loginButton) {
    VerticalLayout layout = new VerticalLayout();
    layout.setSpacing(true);
    layout.setMargin(true);

    layout.addComponent(new Label("Login"));
    userNameField.setWidth(20, Unit.EM);
    passwordField.setWidth(20, Unit.EM);
    layout.addComponent(userNameField);
    layout.addComponent(passwordField);
    layout.addComponent(loginButton);

    addLoginListener(this);

    return layout;
  }

  @Override
  public void onLogin(LoginEvent event) {
    try {
      restClientService.authenticate(event.getLoginParameter("username"),
          event.getLoginParameter("password"));
      Notification.show("Login successful!");
      getUI().getNavigator().navigateTo(MetadataOverviewViewImpl.VIEW_NAME);
    } catch (AuthenticationException ex) {
      Notification.show("Login failed!");
    } catch (ServerException se) {
      Notification.show("Cannot instantiate client HTTPS endpoint");
    }
  }
}
