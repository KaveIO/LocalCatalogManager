/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.kpmg.lcm.common.data.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.kpmg.lcm.common.validation.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 * @author shristov
 */
public class AtlasMetadataDescriptor extends AbstractMetaDataDescriptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AtlasMetadataDescriptor.class);

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
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      String actual = objectMapper.writeValueAsString(body);
      set("body", actual);
    } catch (JsonProcessingException ex) {
      LOGGER.warn("Unable to transform the atlas body map to json string. Error message: "
          + ex.getMessage());
    }
  }

  public final String getLastModifiedTime() {
    return get("last_modified_time");
  }

  public final void setLastModifiedTime(final String lastModifiedTime) {
    set("last_modified_time", lastModifiedTime);
  }

  public final String getStatus() {
    return get("status");
  }

  public final void setStatus(final String status) {
    set("status", status);
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