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

package nl.kpmg.lcm.server.backend;

import nl.kpmg.lcm.common.data.ContentIterator;

import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.SelectItem;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author mhoekstra
 */
class DataSetContentIterator implements ContentIterator {
  private final DataSet dataset;
  private final String[] columns;
  private boolean peeked = false;
  private boolean peekedResponse = false;


  public DataSetContentIterator(DataSet dataset) {
    this.dataset = dataset;

    SelectItem[] selectItems = dataset.getSelectItems();
    columns = new String[selectItems.length];
    for (int i = 0; i < selectItems.length; i++) {
      columns[i] = selectItems[i].getColumn().getName();
    }
  }

  @Override
  public boolean hasNext() {
    if (!peeked) {
      peeked = true;
      peekedResponse = dataset.next();
    }

    return peekedResponse;
  }

  @Override
  public Map next() {
    Row row = dataset.getRow();

    Object[] values = row.getValues();

    Map contentMap = new HashMap();
    for (int i = 0; i < values.length; i++) {
      contentMap.put(columns[i], values[i]);
    }
    peeked = false;
    return contentMap;
  }

  @Override
  public void close() throws IOException {
    dataset.close();
  }

  @Override
  public Iterator iterator() {
    return this;
  }
}
