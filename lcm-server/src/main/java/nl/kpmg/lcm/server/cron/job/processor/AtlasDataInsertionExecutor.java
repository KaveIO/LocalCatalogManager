/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.kpmg.lcm.server.cron.job.processor;

import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.cron.TaskResult;
import nl.kpmg.lcm.server.cron.exception.CronJobExecutionException;
import nl.kpmg.lcm.server.cron.job.AbstractDataProcessor;
import nl.kpmg.lcm.server.data.service.MetaDataService;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 *
 * @author shristov
 */
public class AtlasDataInsertionExecutor extends AbstractDataProcessor {

  @Autowired
  private MetaDataService metadataService;

  @Override
  protected TaskResult execute(MetaDataWrapper metadataWrapper, Map options)
      throws CronJobExecutionException {
    MetaData newMetadata = metadataService.create(metadataWrapper.getMetaData());
    if (newMetadata == null) {
      return TaskResult.FAILURE;
    }
    return TaskResult.SUCCESS;
  }

}
