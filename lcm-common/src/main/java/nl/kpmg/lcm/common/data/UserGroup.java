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

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

import javax.annotation.security.PermitAll;

/**
 * User Group class for storing User Group details.
 *
 * @author venkateswarlub
 *
 */
@PermitAll
@Document(collection = "usergroup")
public class UserGroup extends AbstractModel {

  private String name;

  private List<String> users;

  private List<String> allowedMetadataList;

  private List<String> allowedPathList;

  public boolean addUser(String userName) {
    return users.add(userName);
  }

  public boolean removeUser(String userName) {
    return users.remove(userName);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getUsers() {
    return users;
  }

  public void setUsers(List<String> users) {
    this.users = users;
  }

  public List<String> getAllowedMetadataList() {
    return allowedMetadataList;
  }

  public void setAllowedMetadataList(List<String> allowedMetadataList) {
    this.allowedMetadataList = allowedMetadataList;
  }

  public List<String> getAllowedPathList() {
    return allowedPathList;
  }

  public void setAllowedPathList(List<String> allowedPathList) {
    this.allowedPathList = allowedPathList;
  }
}
