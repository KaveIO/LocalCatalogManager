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
package nl.kpmg.lcm.common.data.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;

import nl.kpmg.lcm.common.validation.Notification;

import java.util.Map;

/**
 *
 * @author shristov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralInfoDescriptor extends AbstractMetaDataDescriptor {
  private String owner;
  private String description;

  public GeneralInfoDescriptor(MetaData metaData) {
    super(metaData);
  }

  public GeneralInfoDescriptor(Map map) {
    super(map);
  }

  @Override
  public String getSectionName() {
    return "general";
  }

  public final String getOwner() {
    return get("owner");
  }

  public final void setOwner(final String Owner) {
    set("owner", Owner);
  }

  public final String getDescription() {
    return get("description");
  }

  public final void setDescription(final String description) {
    set("description", description);
  }

  @Override
  public void validate(Notification notification) {
    if (getMap() == null) {
      notification.addError("Error: Section \"" + getSectionName()
          + "\" is not found in the metadata!");
      return;
    }

    validateField("owner", notification);
    validateField("description", notification);
  }
}
