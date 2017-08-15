/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
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

package nl.kpmg.lcm.server.rest.client.version0.types;

import com.fasterxml.jackson.annotation.JsonIgnore;

import nl.kpmg.lcm.common.rest.types.LinkInjectable;
import nl.kpmg.lcm.common.rest.types.Version0Representation;
import nl.kpmg.lcm.server.rest.client.version0.LocalMetaDataController;
import nl.kpmg.lcm.server.rest.client.version0.RemoteLcmController;
import nl.kpmg.lcm.server.rest.client.version0.StorageController;
import nl.kpmg.lcm.server.rest.client.version0.TaskDescriptionController;
import nl.kpmg.lcm.server.rest.client.version0.TaskScheduleController;
import nl.kpmg.lcm.server.rest.client.version0.UserController;
import nl.kpmg.lcm.server.rest.client.version0.UserGroupController;

import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import java.util.List;

import javax.ws.rs.core.Link;

public class ConcreteVersion0Representation extends Version0Representation
    implements LinkInjectable {

  @InjectLinks({
      @InjectLink(resource = StorageController.class, style = InjectLink.Style.ABSOLUTE,
          rel = "storage.overview"),
      @InjectLink(resource = LocalMetaDataController.class, style = InjectLink.Style.ABSOLUTE,
          rel = "local.overview"),
      @InjectLink(resource = RemoteLcmController.class, style = InjectLink.Style.ABSOLUTE,
          rel = "remote.overview"),
      @InjectLink(resource = TaskDescriptionController.class, style = InjectLink.Style.ABSOLUTE,
          rel = "task.overview"),
      @InjectLink(resource = TaskScheduleController.class, style = InjectLink.Style.ABSOLUTE,
          rel = "taskschedule.overview"),
      @InjectLink(resource = UserController.class, style = InjectLink.Style.ABSOLUTE,
          rel = "users.overview"),
      @InjectLink(resource = UserGroupController.class, style = InjectLink.Style.ABSOLUTE,
          rel = "usergroups.overview")})
  @JsonIgnore
  private List<Link> injectedLinks;

  @Override
  public List<Link> getInjectedLinks() {
    return injectedLinks;
  }
}
