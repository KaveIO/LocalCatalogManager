package nl.kpmg.lcm.rest.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import nl.kpmg.lcm.server.LinksDeserializer;
import nl.kpmg.lcm.server.LinksSerializer;
import nl.kpmg.lcm.server.data.AbstractModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.Link;

/**
 *
 * @author mhoekstra
 */
public abstract class AbstractDataRepresentation<T extends AbstractModel> {

  /**
   * The actual TaskDescription.
   */
  private T item;

  private List<Link> links = new LinkedList();

  public void setItem(T item) {
    this.item = item;
  }

  /**
   * @return the TaskDescription
   */
  public final T getItem() {
    return item;
  }

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

      ArrayList combinedLinks = new ArrayList(injectedLinks);
      combinedLinks.addAll(links);

      return combinedLinks;
    } else {
      return links;
    }
  }
}
