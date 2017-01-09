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

package nl.kpmg.lcm.server.data;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class JsonReaderContentIterator implements ContentIterator {
  private JsonReader reader = null;
  private static final Logger logger = Logger.getLogger(JsonReaderContentIterator.class.getName());
  private final Gson gson = new Gson();

  public JsonReaderContentIterator(JsonReader reader) throws IOException {
    this.reader = reader;
    this.reader.beginArray();
  }

  @Override
  public boolean hasNext() {
    try {
      return reader.hasNext();
    } catch (IOException ex) {
      logger.log(Level.WARNING, "reader.hasNext() threw and exception. {0}", ex.getMessage());
      return false;
    }
  }

  @Override
  public Map next() {
    try {
      Map next = null;
      if (reader.hasNext()) {
        next = gson.fromJson(reader, Map.class);
      }
      return next;
    } catch (IOException ex) {
      logger.log(Level.WARNING,
          "reader.hasNext() threw and exception while reading next element. {0}", ex.getMessage());
      return null;
    }
  }

  @Override
  public void close() throws IOException {
    reader.endArray();
    reader.close();
  }

  @Override
  public Iterator<Map> iterator() {
    return this;
  }
}
