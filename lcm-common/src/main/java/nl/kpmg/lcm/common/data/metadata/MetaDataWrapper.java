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

import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.common.validation.Notification;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shristov
 */
@Wrapper
public class MetaDataWrapper implements MetaDataIdentificator {

  protected final MetaData metaData;
  protected final DataDescriptor dataDescriptor;
  protected final GeneralInfoDescriptor generalInfoDescriptor;
  protected final DynamicDataDescriptor dynamicData;
  protected final EnrichmentPropertiesDescriptor enrichmentPropertiesDescriptor;
  protected final ExpirationTimeDescriptor expirationTimeDescriptor;
  protected final TransferHistoryDescriptor transferHistoryDescriptor;
  protected final AtlasMetadataDescriptor atlasMetadataDescriptor;

  public MetaDataWrapper(MetaData metaData) {
    this.metaData = metaData;
    this.dataDescriptor = new DataDescriptor(metaData);
    this.generalInfoDescriptor = new GeneralInfoDescriptor(metaData);
    this.dynamicData = new DynamicDataDescriptor(metaData);
    this.enrichmentPropertiesDescriptor = new EnrichmentPropertiesDescriptor(metaData);
    this.expirationTimeDescriptor = new ExpirationTimeDescriptor(metaData);
    this.transferHistoryDescriptor = new TransferHistoryDescriptor(metaData);
    this.atlasMetadataDescriptor = new AtlasMetadataDescriptor(metaData);

    Notification notification = new Notification();
    this.validateName(notification);
    this.dataDescriptor.validate(notification);
    this.generalInfoDescriptor.validate(notification);
    this.expirationTimeDescriptor.validate(notification);
    if (notification.hasErrors()) {
      throw new LcmValidationException(notification);
    }
  }

  public MetaDataWrapper() {
    this.metaData = new MetaData();
    this.dataDescriptor = new DataDescriptor(metaData);
    this.generalInfoDescriptor = new GeneralInfoDescriptor(metaData);
    this.dynamicData = new DynamicDataDescriptor(metaData);
    this.enrichmentPropertiesDescriptor = new EnrichmentPropertiesDescriptor(metaData);
    this.expirationTimeDescriptor = new ExpirationTimeDescriptor(metaData);
    this.transferHistoryDescriptor = new TransferHistoryDescriptor(metaData);
    this.atlasMetadataDescriptor = new AtlasMetadataDescriptor(metaData);
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

  public String getSourceType() {
    return getData().getDataType();
  }

  public List<String> getStorageName() {
    List<String> storageNameList = new ArrayList();

    List<String> uriList = getData().getUri();
    for (String uri : uriList) {
      URI parsedUri;
      try {
        if (getData() != null && getData().getUri() != null) {
          parsedUri = new URI(uri);
          String storageName =
              parsedUri.getHost() != null ? parsedUri.getHost() : parsedUri.getAuthority();
          storageNameList.add(storageName);
        }
      } catch (URISyntaxException ex) {
        // TODO log the syntax error Logger.
        continue;
      }
    }
    return storageNameList;
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
    return dynamicData;
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

  /**
   * @return the enrichmentPropertiesDescriptor
   */
  public EnrichmentPropertiesDescriptor getEnrichmentProperties() {
    return enrichmentPropertiesDescriptor;
  }

  /**
   * @param value the EnrichmentPropertiesDescriptor to set
   */
  public void setEnrichmentProperties(EnrichmentPropertiesDescriptor value) {
    metaData.set(value.getSectionName(), value.getMap());
  }

  public final ExpirationTimeDescriptor getExpirationTime() {
    return expirationTimeDescriptor;
  }

  public final void setExpirationTime(final ExpirationTimeDescriptor value) {
    metaData.set(value.getSectionName(), value.getMap());
  }

  public final TransferHistoryDescriptor getTransferHistory() {
    return transferHistoryDescriptor;
  }

  public final void setTransferHistory(final TransferHistoryDescriptor value) {
    metaData.set(value.getSectionName(), value.getTransferHistory());
  }

  public final void validateName(Notification notification) {
    if (getName() == null || getName().isEmpty()) {
      notification.addError("Error: Section \"name\" is not found");
    }
  }

  public final void setAtlasMetadata(AtlasMetadataDescriptor value) {
    metaData.set(value.getSectionName(), value.getMap());
  }

  public final AtlasMetadataDescriptor getAtlasMetadata() {
    return atlasMetadataDescriptor;
  }
}