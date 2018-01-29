/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.kpmg.lcm.common.data.metadata;

import nl.kpmg.lcm.common.validation.Notification;

import java.util.Map;

/**
 *
 * @author shristov
 */
public class AtlasMetadataDescriptor extends AbstractMetaDataDescriptor {
  private MetaData metadata;

  public AtlasMetadataDescriptor(MetaData metadata) {
    super(metadata);
    this.metadata = metadata;
  }

  public AtlasMetadataDescriptor(Map map) {
    super(map);
  }

  @Override
  public String getSectionName() {
    return "atlas";
  }


  public final String getGuid() {
    return get("guid");
  }

  public final void setGuid(final String guid) {
    set("guid", guid);
  }


  public final String getBody() {
    return get("body");
  }

  public final void setBody(final Map body) {
    set("body", body);
  }

  @Override
  public void validate(Notification notification) {
    if (getMap() == null) {
      return;// This section is not madatory and if it missing it is fine.
    }

    if (getGuid() == null || getGuid().isEmpty()) {
      notification.addError("Error: Section " + getSectionName() + ".guid is null or empty!");
    }

    if (getBody() == null || getBody().isEmpty()) {
      notification.addError("Error: Section " + getSectionName() + ".body is null or empty!");
    }
  }
}