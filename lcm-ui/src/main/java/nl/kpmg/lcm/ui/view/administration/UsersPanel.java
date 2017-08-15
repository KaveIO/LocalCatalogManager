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

package nl.kpmg.lcm.ui.view.administration;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import nl.kpmg.lcm.common.rest.types.UserGroupsRepresentation;
import nl.kpmg.lcm.common.rest.types.UsersRepresentation;

/**
 *
 * @author mhoekstra
 */
public class UsersPanel extends CustomComponent {
  private UsersRepresentation users;
  private UserGroupsRepresentation userGroups;

  public UsersPanel() {
    Panel panel = new Panel();
    VerticalLayout root = new VerticalLayout();
    panel.setContent(root);

    root.addComponent(new Label("users"));

    setCompositionRoot(panel);
  }

  public void setUsers(UsersRepresentation users) {
    this.users = users;
  }

  public void setUserGroups(UserGroupsRepresentation userGroups) {
    this.userGroups = userGroups;
  }
}
