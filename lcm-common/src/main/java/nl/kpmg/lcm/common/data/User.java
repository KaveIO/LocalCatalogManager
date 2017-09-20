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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import nl.kpmg.lcm.common.rest.authentication.PasswordHash;
import nl.kpmg.lcm.common.rest.authentication.UserPasswordHashException;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

import javax.persistence.Transient;

/**
 * User class to store User details.
 *
 */
@JsonIgnoreProperties({"hashed", "password"})
@Document(collection = "user")
public class User extends AbstractModel {
  public final static String LOCAL_ORIGIN = "local";

  private String name;

  @JsonIgnore
  private String password;

  private String role;

  @Transient
  private boolean hashed = false;

  @Transient
  private String newPassword;

  private String origin;

  private List<String> allowedMetadataList;

  private List<String> allowedPathList;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPassword(String password) throws UserPasswordHashException {
    this.setPassword(password, false);
  }

  @JsonProperty
  public void setPassword(String password, boolean hashPassword) throws UserPasswordHashException {
    if (hashPassword) {
      this.password = PasswordHash.createHash(password);
      this.hashed = true;
    } else {
      this.password = password;
      this.hashed = false;
    }
  }

  /**
   * Returns the password of the user. The User object is used for both the data model and in the
   * Rest interface representation. The javax.annotation.security.DenyAll is used for controlling
   * the visibility of the fields in the Rest interface.
   *
   * @return the password of the user.
   */
  @JsonIgnore
  public String getPassword() {
    return password;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public boolean passwordEquals(String password) throws UserPasswordHashException {
    if (this.password != null && password != null) {
      if (hashed) {
        return PasswordHash.validatePassword(password, this.password);
      } else {
        return this.password.equals(password);
      }
    }
    return false;
  }

  public boolean isHashed() {
    return hashed;
  }

  public void setHashed(boolean hashed) {
    this.hashed = hashed;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  /**
   * @return the origin
   */
  public String getOrigin() {
    return origin;
  }

  /**
   * @param origin the origin to set
   */
  public void setOrigin(String origin) {
    this.origin = origin;
  }

  /**
   * @return the allowedMetadataList
   */
  public List<String> getAllowedMetadataList() {
    return allowedMetadataList;
  }

  /**
   * @param allowedMetadataList the allowedMetadataList to set
   */
  public void setAllowedMetadataList(List<String> allowedMetadataList) {
    this.allowedMetadataList = allowedMetadataList;
  }

  /**
   * @return the allowedPathList
   */
  public List<String> getAllowedPathList() {
    return allowedPathList;
  }

  /**
   * @param allowedPathList the allowedPathList to set
   */
  public void setAllowedPathList(List<String> allowedPathList) {
    this.allowedPathList = allowedPathList;
  }
}
