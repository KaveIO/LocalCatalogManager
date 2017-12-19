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
import nl.kpmg.lcm.common.data.metadata.DataItemsDescriptor;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendCsvImpl;
import nl.kpmg.lcm.server.backend.BackendFileImpl;
import nl.kpmg.lcm.server.backend.BackendHiveImpl;
import nl.kpmg.lcm.server.backend.BackendJsonImpl;
import nl.kpmg.lcm.server.backend.BackendMongoImpl;

import java.util.Map;

/**
 *
 * @author shristov
 */
public class DataDeleter {

  private MetaData metadata;
  private StorageService storageService;
  private TaskDescriptionService taskDescriptionService;
  private String taskId;

  public DataDeleter(StorageService storageService,
      TaskDescriptionService taskDescriptionService, MetaData metadata, String taskId) {

    this.storageService = storageService;
    this.taskDescriptionService = taskDescriptionService;
    this.metadata = metadata;
    this.taskId = taskId;
  }

  public void execute() {
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
    }
    backend.enrichMetadata(EnrichmentProperties.createDataExistingEnrichmentProperties());
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
