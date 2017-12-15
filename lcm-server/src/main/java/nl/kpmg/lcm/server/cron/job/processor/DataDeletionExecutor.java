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
package nl.kpmg.lcm.server.cron.job.processor;

import nl.kpmg.lcm.common.data.ProgressIndication;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.cron.TaskResult;
import nl.kpmg.lcm.server.cron.exception.CronJobExecutionException;
import nl.kpmg.lcm.server.cron.job.AbstractDataProcessor;
import nl.kpmg.lcm.server.data.service.RemoteDataDeletionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 *
 * @author shristov
 */
public class DataDeletionExecutor extends AbstractDataProcessor {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(DataDeletionExecutor.class.getName());

  @Autowired
  RemoteDataDeletionService service;

  @Override
  protected TaskResult execute(MetaDataWrapper metadataWrapper, Map options)
      throws CronJobExecutionException {
    taskDescriptionService.updateProgress(taskId, new ProgressIndication(
        "Deletion started successfully!"));
    service.deleteData(metadataWrapper.getMetaData(), taskId);
    return TaskResult.SUCCESS;
  }

}
