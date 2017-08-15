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

package nl.kpmg.lcm.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.Link;

/**
 * Inner static class for serializing Link objects.
 */
public class LinksSerializer extends JsonSerializer<List<Link>> {

  @Override
  public final void serialize(final List<Link> links, final JsonGenerator jg,
      final SerializerProvider sp) throws IOException {

    if (!links.isEmpty()) {
      jg.writeStartArray();
      for (Link link : links) {
        jg.writeStartObject();
        jg.writeStringField("rel", link.getRel());
        jg.writeStringField("href", link.getUri().toString());
        if (link.getType() != null) {
          jg.writeStringField("type", link.getType());
        }
        jg.writeEndObject();
      }
      jg.writeEndArray();
    }
  }

}
