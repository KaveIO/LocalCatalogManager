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

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "remote_lcm")
public class RemoteLcm extends AbstractModel {
  /**
   * The domain to contact the lcm described here .
   */
  private String name;

  /**
   * The domain to contact the lcm described here .
   */
  private String domain;

  /**
   * The protocol - HTTP or HTTPS
   */
  private String protocol;

  /**
   * The protocol - HTTP or HTTPS
   */
  private Integer port;

  private String status;

  private String uniqueId;

  private String applicationId;

  private String applicationKey;

  /**
   * @return the domain
   */
  public String getDomain() {
    return domain;
  }

  /**
   * @param domain the domain to set
   */
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * @return the protocol
   */
  public String getProtocol() {
    return protocol;
  }

  /**
   * @param protocol the protocol to set
   */
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  /**
   * @return the port
   */
  public Integer getPort() {
    return port;
  }

  /**
   * @param port the port to set
   */
  public void setPort(Integer port) {
    this.port = port;
  }

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
   * @return the lastStatus
   */
  public String getStatus() {
    return status;
  }

  /**
   * @param status the lastStatus to set
   */
  public void setStatus(String status) {
    this.status = status;
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
