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

import nl.kpmg.lcm.common.NamespacePathValidator;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.common.validation.Notification;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
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

  public final List<String> getUri() {
    return get("uri");
  }

  public final void setUri(final List<String> uriList) {
    set("uri", uriList);
  }

  public final void addUri(final String uri) {
    List<String> list = (List<String>) get("uri");
    list.add(uri);
    set("uri", list);
  }

  public final String getPath() {
    return get("path");
  }

  public final void setPath(final String path) {
    set("path", path);
  }

  public List<String> getStorageItemName() {
    List<String> uriList = getUri();
    List<String> pathList = new ArrayList();
    for (String uri : uriList) {
      try {
        URI dataUri = new URI(uri);
        String path = dataUri.getPath();
        pathList.add(path);
      } catch (URISyntaxException ex) {
        throw new LcmException(String.format("Failure while trying to parse URI '%s'", uri),
            ex);
      }
    }

    return pathList;
  }

  public String getDataType() {
    try {
      List<String> uriList = getUri();
      if (uriList != null && !uriList.isEmpty()) {
        URI dataUri = new URI(uriList.get(0));
        return dataUri.getScheme();
      }
      throw new LcmException(String.format("Data URL list is empy or it does not exiosts!"));
    } catch (URISyntaxException ex) {
      throw new LcmException("Failure while trying to parse URI list", ex);
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

    validateField("path", notification);
    String path = (String) getMap().get("path");
    NamespacePathValidator validator = new NamespacePathValidator();
    validator.validate(path, notification);
  }
}
