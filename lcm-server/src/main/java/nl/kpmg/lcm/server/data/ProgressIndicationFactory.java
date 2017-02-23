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
package nl.kpmg.lcm.server.data;

import nl.kpmg.lcm.server.data.service.TaskDescriptionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 *
 * @author shristov
 */
public class ProgressIndicationFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProgressIndicationFactory.class
      .getName());


  private TaskDescriptionService taskService;
  private String taskId;
  private int indicationChunkSize;

  public ProgressIndicationFactory(TaskDescriptionService taskService, String taskId,
      int indicationChunkSize) {
    this.taskService = taskService;
    this.taskId = taskId;
    this.indicationChunkSize = indicationChunkSize;
  }

  /**
   * @return the indicationChunckSize
   */
  public int getIndicationChunkSize() {
    return indicationChunkSize;
  }

  public void writeIndication(String message) {
    ProgressIndication indication;
    indication = new ProgressIndication(message, new Date());
    taskService.updateProgress(taskId, indication);
    LOGGER.info(message);
  }
}
