package nl.kpmg.lcm.rest.types;

import nl.kpmg.lcm.server.data.AbstractModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mhoekstra
 */
public abstract class AbstractDatasRepresentation<T extends AbstractDataRepresentation>
    extends AbstractRepresentation {

  private static final Logger logger =
      Logger.getLogger(AbstractDatasRepresentation.class.getName());

  /**
   * The actual TaskDescription.
   */
  private List<T> items = new LinkedList();

  public void setItems(List<T> items) {
    this.items = items;
  }

  public void addRepresentedItems(Class type, List<AbstractModel> items) {
      setRepresentedItems(type,  items,  true);
  }

  public void setRepresentedItems(Class type, List<AbstractModel> items) {
      setRepresentedItems(type,  items,  false);
  }

  private void setRepresentedItems(Class type, List<AbstractModel> items,  boolean update) {
    // Madness? Perhaps... with a bit of love (and defensive programming...) there is some hope to
    // salvage this. This is a lot of risky effort to save code duplication.
    if(this.items ==  null || update == false) {
        this.items = new ArrayList();
    }
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
}
