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
