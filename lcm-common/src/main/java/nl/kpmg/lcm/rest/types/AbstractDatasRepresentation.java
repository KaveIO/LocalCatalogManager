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

import nl.kpmg.lcm.server.data.AbstractModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author mhoekstra
 */
public abstract class AbstractDatasRepresentation<T extends AbstractDataRepresentation>
    extends AbstractRepresentation {

  private static final Logger logger =
      LoggerFactory.getLogger(AbstractDatasRepresentation.class.getName());

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
        logger.warn(String.format("Couldn't instantiate represented item. Of type: %s",
            type.getTypeName()));
      }
    } catch (InstantiationException | IllegalAccessException ex) {
      logger.warn(String.format("Couldn't instantiate represented item. Of type: %s",
          type.getTypeName()));
    }
  }

  /**
   * @return the TaskDescription
   */
  public final List<T> getItems() {
    return items;
  }
}
