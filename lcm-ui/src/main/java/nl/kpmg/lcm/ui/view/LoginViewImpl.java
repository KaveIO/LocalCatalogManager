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

import com.ejt.vaadin.loginform.DefaultVerticalLoginForm;
import com.ejt.vaadin.loginform.LoginForm;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.RestClientService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author mhoekstra
 */
@Component
@Scope("prototype")
@UIScope
@VaadinView(LoginViewImpl.VIEW_NAME)
public class LoginViewImpl extends VerticalLayout implements LoginView {

  public static final String VIEW_NAME = "";

  @Autowired
  private RestClientService restClientService;

  public LoginViewImpl() {
    setMargin(true);
    Label header = new Label("Login");
    addComponent(header);

    DefaultVerticalLoginForm loginForm = new DefaultVerticalLoginForm();
    loginForm.addLoginListener(new LoginForm.LoginListener() {
      @Override
      public void onLogin(LoginForm.LoginEvent event) {
        try {
          restClientService.authenticate(event.getUserName(), event.getPassword());
          Notification.show("Login successful!");
          getUI().getNavigator().navigateTo(MetadataOverviewViewImpl.VIEW_NAME);
        } catch (AuthenticationException ex) {
          Notification.show("Login failed!");
        } catch (ServerException se) {
          Notification.show("Cannot instantiate client HTTPS endpoint");
        }
      }
    });

    addComponent(loginForm);
  }

  @Override
  public void enter(ViewChangeListener.ViewChangeEvent event) {
    if (restClientService.isAuthenticated()) {
      getUI().getNavigator().navigateTo(MetadataOverviewViewImpl.VIEW_NAME);
    }
  }
}
