/*
 * Copyright 2016 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.kpmg.lcm.server.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.persistence.Index;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "fetch_endpoint")
public class FetchEndpoint extends AbstractModel {

  /**
   * Foreign key
   */
  private String metadataID;

  /**
   * The creation date
   */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.S")
  private Date creationDate;
  /**
   * the time to live
   */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.S")
  private Date timeToLive;

  /**
   * This is the username/caller that is allowed to use this fetch url
   */
  private String userToConsume;

  /**
   * @return the metadataID
   */
  public String getMetadataId() {
    return metadataID;
  }

  /**
   * @param metadataID the metadataID to set
   */
  public void setMetadataId(String metadataID) {
    this.metadataID = metadataID;
  }

  /**
   * @return the creationDate
   */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * @param creationDate the creationDate to set
   */
  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * @return the timeToLive
   */
  public Date getTimeToLive() {
    return timeToLive;
  }

  /**
   * @param timeToLive the timeToLive to set
   */
  public void setTimeToLive(Date timeToLive) {
    this.timeToLive = timeToLive;
  }

  /**
   * @return the userToConsume
   */
  public String getUserToConsume() {
    return userToConsume;
  }

  /**
   * @param userToConsume the userToConsume to set
   */
  public void setUserToConsume(String userToConsume) {
    this.userToConsume = userToConsume;
  }

}
