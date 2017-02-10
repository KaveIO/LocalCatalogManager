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
package nl.kpmg.lcm.server.data.meatadata;

import nl.kpmg.lcm.validation.Notification;

import java.util.Map;

/**
 *
 * @author shristov
 */
public class DataDescriptor extends AbstractMetaDataDescriptor{

   DataDescriptor(MetaData metaData) {
    super(metaData);
  }

  public final String getUri() {
    return get("uri");
  }

  public final void setUri(final String dataUri) {
      set("uri", dataUri);
  }

  public final Map getOptions() {
    return get("options");
  }

  public final void setOptions(final Map dataOptions) {
    set("options", (Object) dataOptions);
  }

    @Override
    public String getSectionName() {
        return "data";
    }

    @Override
    public void validate(Notification notification) {
        if(getMap() == null) {
            notification.addError("Error: Section \"" + getSectionName() + "\" is not found in the metadata!");
            return;
        }
        validateField("uri", notification);
        validateField("options", notification);
    }
}
