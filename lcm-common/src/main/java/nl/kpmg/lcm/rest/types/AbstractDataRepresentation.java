package nl.kpmg.lcm.rest.types;

import nl.kpmg.lcm.server.data.AbstractModel;


/**
 *
 * @author mhoekstra
 */
public abstract class AbstractDataRepresentation<T extends AbstractModel>
    extends AbstractRepresentation {

  /**
   * The actual TaskDescription.
   */
  private T item;

  public void setItem(T item) {
    this.item = item;
  }

  /**
   * @return the TaskDescription
   */
  public final T getItem() {
    return item;
  }
}
