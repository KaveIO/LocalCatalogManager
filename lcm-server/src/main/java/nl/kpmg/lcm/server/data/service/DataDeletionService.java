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

import nl.kpmg.lcm.common.data.metadata.MetaData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author shristov
 */
@Service
public class DataDeletionService {

  @Autowired
  private MetaDataService metadataService;

  @Autowired
  private StorageService storageService;

  @Autowired
  private TaskDescriptionService taskDescriptionService;

  public void deleteData(MetaData metadata, String taskId) {
    DataDeleter deleter =
        new DataDeleter(metadataService, storageService, taskDescriptionService, metadata, taskId);
    deleter.execute();
  }

  public void deleteDataByThread(MetaData metadata) {
     DataDeleter deleter =
        new DataDeleter(metadataService, storageService, taskDescriptionService, metadata, null);

    DataDeleterThread deleterThread =
        new DataDeleterThread(deleter);
    deleterThread.start();
  }
}