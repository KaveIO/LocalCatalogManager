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
package nl.kpmg.lcm.server.data.service;

import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.EnrichmentProperties;
import nl.kpmg.lcm.common.data.ProgressIndication;
import nl.kpmg.lcm.common.data.TaskDescription;
import nl.kpmg.lcm.common.data.metadata.DataItemsDescriptor;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendCsvImpl;
import nl.kpmg.lcm.server.backend.BackendFileImpl;
import nl.kpmg.lcm.server.backend.BackendHiveImpl;
import nl.kpmg.lcm.server.backend.BackendJsonImpl;
import nl.kpmg.lcm.server.backend.BackendMongoImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 * @author shristov
 */
public class RemoteDataDeleter {

  private final Logger LOGGER = LoggerFactory.getLogger(RemoteDataDeleter.class.getName());

  private MetaDataService metadataService;
  private StorageService storageService;
  private TaskDescriptionService taskDescriptionService;

  private MetaData metadata;
  private String taskId;

  public RemoteDataDeleter(MetaDataService metadataService, StorageService storageService,
      TaskDescriptionService taskDescriptionService, MetaData metadata, String taskId) {
    this.metadataService = metadataService;
    this.storageService = storageService;
    this.taskDescriptionService = taskDescriptionService;
    this.metadata = metadata;
    this.taskId = taskId;
  }

  public void deleteRemoteData() {
    Backend backend = getBackend();
    MetaDataWrapper wrapper = new MetaDataWrapper(metadata);
    Map<String, DataItemsDescriptor> map = wrapper.getDynamicData().getAllDynamicDataDescriptors();
    if (map == null) {
      return;
    }
    for (String key : map.keySet()) {
      backend.delete(key);
    }

    if (taskId != null) {
      taskDescriptionService.updateProgress(taskId, new ProgressIndication(
          "Deletion finished successfully!"));

      TaskDescription task = taskDescriptionService.findOne(taskId);
      EnrichmentProperties enrichment;
      enrichment = wrapper.getEnrichmentProperties().getEnrichmentProperties();

      // metadata enrichment is empty and there is storage enrichment section
      if (enrichment == null && task.getOptions() != null) {
        enrichment = new EnrichmentProperties(task.getOptions());
      }

      // If no preset enrichment properties are found then set the default enrichment properties.
      if (enrichment == null) {
        enrichment = EnrichmentProperties.createDefaultEnrichmentProperties();
      }

      try {
        metadataService.enrichMetadata(wrapper, enrichment);
      } catch (Exception e) {
        LOGGER.error("Unableto enrich metadata. Metadata id: " + wrapper.getId());
      }
    }
  }

  private Backend getBackend() {
    MetaDataWrapper wrapper = new MetaDataWrapper(metadata);
    String sourceType = wrapper.getSourceType();
    Backend backend = null;

    if (sourceType.equals(DataFormat.FILE) || sourceType.equals(DataFormat.S3FILE)
        || sourceType.equals(DataFormat.HDFSFILE) || sourceType.equals(DataFormat.AZUREFILE)) {
      backend = new BackendFileImpl(metadata, storageService);
    }

    if (sourceType.equals(DataFormat.CSV) || sourceType.equals(DataFormat.AZURECSV)) {
      backend = new BackendCsvImpl(metadata, storageService);
    }

    if (sourceType.equals(DataFormat.JSON)) {
      backend = new BackendJsonImpl(metadata, storageService);
    }

    if (sourceType.equals(DataFormat.HIVE)) {
      backend = new BackendHiveImpl(metadata, storageService);
    }

    if (sourceType.equals(DataFormat.MONGO)) {
      backend = new BackendMongoImpl(metadata, storageService);
    }
    return backend;
  }
}
