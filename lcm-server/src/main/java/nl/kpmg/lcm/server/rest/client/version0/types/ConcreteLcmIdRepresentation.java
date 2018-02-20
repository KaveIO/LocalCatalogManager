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
package nl.kpmg.lcm.server.rest.client.version0.types;

import com.fasterxml.jackson.annotation.JsonIgnore;

import nl.kpmg.lcm.common.data.LcmId;
import nl.kpmg.lcm.common.rest.types.LcmIdRepresentation;
import nl.kpmg.lcm.common.rest.types.LinkInjectable;
import nl.kpmg.lcm.server.rest.client.version0.LcmIdController;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import java.util.List;

import javax.ws.rs.core.Link;

/**
 *
 * @author shristov
 */
public class ConcreteLcmIdRepresentation extends LcmIdRepresentation  implements LinkInjectable {


  @InjectLinks({@InjectLink(resource = LcmIdController.class,
      style = InjectLink.Style.ABSOLUTE, rel = "slef", method = "getOne", bindings = {@Binding(
          name = "id", value = "${instance.item.id}")})})
  @JsonIgnore
  private List<Link> injectedLinks;


  @Override
  public List<Link> getInjectedLinks() {
    return injectedLinks;
  }

  public ConcreteLcmIdRepresentation() {};

  public ConcreteLcmIdRepresentation(LcmId lcmid) {
    setItem(lcmid);
  }
}
