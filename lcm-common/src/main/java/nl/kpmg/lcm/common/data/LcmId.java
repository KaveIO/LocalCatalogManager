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
package nl.kpmg.lcm.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 *
 * @author shristov
 */
@Document(collection = "lcmId")
public class LcmId extends AbstractModel {
  @Field("lcm-id")
  private String lcmId;

  /**
   * @return the lcmId
   */
  @JsonProperty("lcm-id")
  @Field("lcm-id")
  public String getLcmId() {
    return lcmId;
  }

  /**
   * @param lcmid the lcmId to set
   */
  @JsonProperty("lcm-id")
  @Field("lcm-id")
  public void setLcmId(String lcmid) {
    this.lcmId = lcmid;
  }

}