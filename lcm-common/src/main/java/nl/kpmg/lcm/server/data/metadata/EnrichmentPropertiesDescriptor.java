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

import nl.kpmg.lcm.server.data.EnrichmentProperties;
import nl.kpmg.lcm.validation.Notification;

import java.util.List;
import java.util.Set;

/**
 *
 * @author shristov
 */
public class EnrichmentPropertiesDescriptor extends AbstractMetaDataDescriptor {
  public EnrichmentPropertiesDescriptor(MetaData metaData) {
    super(metaData);
  }

  @Override
  public String getSectionName() {
    return "enrichment-properties";
  }

  @Override
  public void validate(Notification notification) {
    //Intentialy blank as there are no manadtory properties.
  }

  public final String getCron() {
    return get("cron-expression");
  }

  public final void setCron(final String cron) {
    set("cron-expression", cron);
  }

  public final EnrichmentProperties getEnrichmentProperties() {
    if (getMap() == null) {
      return null;
    }
    return new EnrichmentProperties(getMap());
  }

  public final List<String> getCollectedProperties() {
    return get("collected-properties");
  }

  public final void setCollectedProperties(final Set<String> properties) {
    set("collected-properties", properties);
  }

}
