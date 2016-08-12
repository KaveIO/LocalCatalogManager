package nl.kpmg.lcm.rest.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import nl.kpmg.lcm.server.LinksDeserializer;
import nl.kpmg.lcm.server.LinksSerializer;
import nl.kpmg.lcm.server.data.AbstractModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Link;

/**
 *
 * @author mhoekstra
 */
public abstract class AbstractDatasRepresentation<T extends AbstractDataRepresentation> {

  private static final Logger logger =
      Logger.getLogger(AbstractDatasRepresentation.class.getName());

  /**
   * The actual TaskDescription.
   */
  private List<T> items = new LinkedList();

  private List<Link> links = new LinkedList();

  public void setItems(List<T> items) {
    this.items = items;
  }


  public void setRepresentedItems(Class type, List<AbstractModel> items) {
    // Madness? Perhaps... with a bit of love (and defensive programming...) there is some hope to
    // salvage this. This is a lot of risky effort to save code duplication.
    this.items = new ArrayList();
    try {
      if (AbstractDataRepresentation.class.isAssignableFrom(type)) {
        Class<AbstractDataRepresentation> castedType = (Class<AbstractDataRepresentation>) type;
        for (AbstractModel item : items) {
          T newDataRepresentation = (T) castedType.newInstance();
          newDataRepresentation.setItem(item);
          this.items.add(newDataRepresentation);
        }
      } else {
        logger.log(Level.WARNING, "Couldn't instantiate represented item. Of type: {0}",
            type.getTypeName());
      }
    } catch (InstantiationException | IllegalAccessException ex) {
      logger.log(Level.WARNING, "Couldn't instantiate represented item. Of type: {0}",
          type.getTypeName());
    }
  }

  /**
   * @return the TaskDescription
   */
  public final List<T> getItems() {
    return items;
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

      if (injectedLinks != null) {
        ArrayList combinedLinks = new ArrayList(injectedLinks);
        combinedLinks.addAll(links);

        return combinedLinks;
      }
    }
    return links;
  }
}
