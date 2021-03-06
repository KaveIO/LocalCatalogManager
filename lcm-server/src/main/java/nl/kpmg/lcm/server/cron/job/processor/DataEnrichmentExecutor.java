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

package nl.kpmg.lcm.server.cron.job.processor;

import nl.kpmg.lcm.server.cron.job.AbstractDataProcessor;
import nl.kpmg.lcm.common.data.EnrichmentProperties;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.cron.exception.CronJobExecutionException;
import nl.kpmg.lcm.server.cron.TaskResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Fills data on a MetaData object concerning the described Data.
 *
 * <p>
 * MetaData describes data. This Task will check through the appropriate backend if the data in
 * question is actually attached and/or readable from the LCM. This will update and overwrite the
 * specific piece of MetaData. The data added contains: - state : DETACHED | ATTACHED - readable :
 * UNREADABLE | READABLE - size : byte-size - update-timestamp : date
 * </p>
 * <p>
 * this will be set on the following path:
 * </p>
 * <p>
 * { "dynamic": { "data" { "state" "readable" "size" "update-timestamp" } } }
 * </p>
 *
 * @author mhoekstra
 */
public class DataEnrichmentExecutor extends AbstractDataProcessor {

  private final Logger LOGGER = LoggerFactory.getLogger(DataEnrichmentExecutor.class.getName());
  /**
   * The BackendService.
   */
  @Autowired
  private MetaDataService metaDataService;

  /**
   * Will store information on the data associated with a piece of MetaData.
   *
   * @param metadata the metadata to enrich
   * @param options - any option that could be useful during task execution
   * @return the result of the task
   * @throws CronJobExecutionException if the backend fails
   */
  @Override
  protected final TaskResult execute(final MetaDataWrapper metadataWrapper, final Map options)
      throws CronJobExecutionException {
    EnrichmentProperties enrichment;

    enrichment = metadataWrapper.getEnrichmentProperties().getEnrichmentProperties();

    // metadata enrichment is empty and there is storage enrichment section
    if (enrichment == null && options != null) {
      enrichment = new EnrichmentProperties(options);
    }

    //If no preset enrichment properties are found then set the default enrichment properties.
    if (enrichment == null) {
      enrichment = EnrichmentProperties.createDefaultEnrichmentProperties();
    }

    boolean result = false;
    try {
      result = metaDataService.enrichMetadata(metadataWrapper, enrichment);
    } catch (Exception e) {
      LOGGER.error("Unableto enrich metadata. Metadata id: " + metadataWrapper.getId());
    }

    return result ? TaskResult.SUCCESS : TaskResult.FAILURE;
  }
}
