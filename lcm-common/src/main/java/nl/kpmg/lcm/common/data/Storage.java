/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
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
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

import javax.annotation.security.PermitAll;

/**
 *
 * @author kos
 */
@Document(collection = "storage")
@PermitAll
public class Storage extends AbstractModel {

  @Indexed(unique = true)
  private String name;

  private String type;

  private Map options;

  private Map credentials;

  private String status;

  @Field("enrichment-properties")
  private Map enrichmentProperties;

  /**
   *
   * @return options
   */
  public Map getOptions() {
    return options;
  }

  public void setOptions(Map options) {
    this.options = options;
  }

  /** This section is treated in a different way. It must never be exposed outside of the
   * application through the REST api or any other way.
   *
   * @return credentials
   */
  @JsonIgnore
  public Map getCredentials() {
    return credentials;
  }

  /**
   * This section is treated in a different way. It is never exposed outside of the application
   * through the REST api.
   *
   * @param credentials
   */
  @JsonProperty
  public void setCredentials(Map credentials) {
    this.credentials = credentials;
  }

  /**
   *
   * @return enrichment-properties
   */
  @JsonProperty("enrichment-properties")
  @Field("enrichment-properties")
  public Map getEnrichmentProperties() {
    return enrichmentProperties;
  }

  @JsonProperty("enrichment-properties")
  /* this annotation is needed for jersey */
  @Field("enrichment-properties")
  /* this annotation is needed for mongoDB */
  public void setEnrichmentProperties(Map enrichmentProperties) {
    this.enrichmentProperties = enrichmentProperties;
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
   * @return the storage type e. g. "csv", "hive" etc.
   */
  public String getType() {
    return type;
  }

  /**
   * @param type - sets the storage type e. g. "csv", "hive" etc.
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * @param status the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }
}
