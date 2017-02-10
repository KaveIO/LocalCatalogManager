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

import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.validation.Notification;

/**
 *
 * @author shristov
 */
public class MetaDataWrapper implements MetaDataIdentificator {

  protected final MetaData metaData;
  protected final DataDescriptor dataDescriptor;
  protected final GeneralInfoDescriptor generalInfoDescriptor;
  protected final DynamicDataDescriptor dynamicDataDescriptor;

  public MetaDataWrapper(MetaData metaData) {
    this.metaData = metaData;
    this.dataDescriptor = new DataDescriptor(metaData);
    this.generalInfoDescriptor = new GeneralInfoDescriptor(metaData);
    this.dynamicDataDescriptor = new DynamicDataDescriptor(metaData);

    Notification notification = new Notification();
    this.dataDescriptor.validate(notification);
    this.generalInfoDescriptor.validate(notification);

    if (notification.hasErrors()) {
      throw new LcmValidationException(notification);
    }
  }

  public MetaDataWrapper() {
    this.metaData = new MetaData();
    this.dataDescriptor = new DataDescriptor(metaData);
    this.generalInfoDescriptor = new GeneralInfoDescriptor(metaData);
    this.dynamicDataDescriptor = new DynamicDataDescriptor(metaData);
  }

  /**
   * @return the id of the object
   */
  public final String getId() {
    return metaData.getId();
  }

  /**
   * @param id the unique id of the object
   */
  public final void setId(final String id) {
    metaData.setId(id);
  }

  @Override
  public String getSourceType() {
    return metaData.getSourceType();
  }

  @Override
  public void setSourceType(String sourceType) {
    metaData.setSourceType(sourceType);
  }

  @Override
  public String getName() {
    return metaData.getName();
  }

  @Override
  public void setName(String name) {
    metaData.setName(name);
  }

  public final void setData(final DataDescriptor value) {
    metaData.set(value.getSectionName(), value.getMap());
  }

  public final DataDescriptor getData() {
    return dataDescriptor;
  }

  public final void setDynamicData(final DynamicDataDescriptor value) {
    metaData.set(value.getSectionName(), value.getMap());
  }

  public final DynamicDataDescriptor getDynamicData() {
    return dynamicDataDescriptor;
  }

  public final GeneralInfoDescriptor getGeneralInfo() {
    return generalInfoDescriptor;
  }

  public final void setGeneralInfo(GeneralInfoDescriptor value) {
    metaData.set(value.getSectionName(), value.getMap());
  }

  /**
   * @return the metaData
   */
  public MetaData getMetaData() {
    return metaData;
  }

  public boolean isEmpty() {
    return metaData == null;
  }
}
