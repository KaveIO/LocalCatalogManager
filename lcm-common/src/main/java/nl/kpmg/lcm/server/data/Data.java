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
package nl.kpmg.lcm.server.data;

import nl.kpmg.lcm.server.data.metadata.MetaData;

/**
 *
 * @author shristov
 */
public class Data {
  private final MetaData metaData;

  public Data(MetaData metaData) {
    this.metaData = metaData;
  }

  /**
   * @return the metaData
   */
  public MetaData getMetaData() {
    return metaData;
  }
}
