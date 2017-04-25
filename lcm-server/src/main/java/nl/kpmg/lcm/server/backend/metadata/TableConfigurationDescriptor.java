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
package nl.kpmg.lcm.server.backend.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;

import nl.kpmg.lcm.server.data.metadata.AbstractMetaDataDescriptor;
import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.validation.Notification;

/**
 *
 * @author shristov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableConfigurationDescriptor extends AbstractMetaDataDescriptor {

  public TableConfigurationDescriptor(MetaData metaData) {
    super(metaData);
  }

  public final String getEncoding() {
    return get("encoding");
  }

  public final void setEncoding(final String encoding) {
    set("encoding", encoding);
  }

  @Override
  public String getSectionName() {
    return "data.options.table-configuration";
  }

  @Override
  public void validate(Notification notification) {
      //Initntionally blank as for now this "table-configuration" section is not mandatory
  }

}
