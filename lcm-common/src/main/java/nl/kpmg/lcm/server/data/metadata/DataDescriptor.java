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
package nl.kpmg.lcm.server.data.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;

import nl.kpmg.lcm.server.exception.LcmException;
import nl.kpmg.lcm.validation.Notification;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 *
 * @author shristov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataDescriptor extends AbstractMetaDataDescriptor {

  public DataDescriptor(MetaData metaData) {
    super(metaData);
  }

  public DataDescriptor(Map map) {
    super(map);
  }

  public final String getUri() {
    return get("uri");
  }

  public final void setUri(final String uri) {
    set("uri", uri);
  }

  public String getStorageItemName() {
    try {
      URI dataUri = new URI(getUri());
      return dataUri.getPath();
    } catch (URISyntaxException ex) {
      throw new LcmException(String.format("Failure while trying to parse URI '%s'", getUri()), ex);
    }
  }

  public String getDataType() {
    try {
      URI dataUri = new URI(getUri());
      return dataUri.getScheme();
    } catch (URISyntaxException ex) {
      throw new LcmException(String.format("Failure while trying to parse URI '%s'", getUri()), ex);
    }
  }

  @Override
  public String getSectionName() {
    return "data";
  }

  @Override
  public void validate(Notification notification) {
    if (getMap() == null) {
      notification.addError("Error: Section \"" + getSectionName()
          + "\" is not found in the metadata!");
      return;
    }

    validateField("uri", notification);
  }
}
