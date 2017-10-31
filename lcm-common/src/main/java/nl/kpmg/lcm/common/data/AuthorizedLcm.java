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

package nl.kpmg.lcm.common.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "authorizedLcm")
public class AuthorizedLcm extends AbstractModel {
  /**
   * The domain to contact the lcm described here .
   */
  private String name;

  @Indexed(name = "unique-lcm-id", unique = true)
  private String uniqueId;

  /**
   * used like username
   */
  private String applicationId;

  /**
   * Used like password
   */
  private String applicationKey;

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the uniqueId
   */
  public String getUniqueId() {
    return uniqueId;
  }

  /**
   * @param uniqueId the uniqueId to set
   */
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  /**
   * @return the applicationId
   */
  public String getApplicationId() {
    return applicationId;
  }

  /**
   * @param applicationId the applicationId to set
   */
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   * @return the applicationKey
   */
  @JsonIgnore
  public String getApplicationKey() {
    return applicationKey;
  }

  /**
   * @param applicationKey the applicationKey to set
   */
  @JsonProperty
  public void setApplicationKey(String applicationKey) {
    this.applicationKey = applicationKey;
  }
}
