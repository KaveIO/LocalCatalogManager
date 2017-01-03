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

package nl.kpmg.lcm.rest.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import nl.kpmg.lcm.server.LinksDeserializer;
import nl.kpmg.lcm.server.LinksSerializer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.Link;

/**
 *
 * @author mhoekstra
 */
public abstract class AbstractRepresentation {

  private List<Link> links = new LinkedList();

  @JsonDeserialize(using = LinksDeserializer.class)
  public final void setLinks(final List<Link> links) {
    this.links = links;
  }

  /**
   * @return the list of Links
   */
  @JsonSerialize(using = LinksSerializer.class)
  public final List<Link> getLinks() {
    if (LinkInjectable.class.isAssignableFrom(getClass())) {
      List<Link> injectedLinks = ((LinkInjectable) this).getInjectedLinks();

      if (injectedLinks != null) {
        ArrayList combinedLinks = new ArrayList(injectedLinks);
        combinedLinks.addAll(links);

        return combinedLinks;
      }
    }
    return links;
  }
}
