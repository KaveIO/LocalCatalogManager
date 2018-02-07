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

import java.util.List;
import java.util.Map;

/**
 *
 * @author shristov
 */
public class AtlasDataUpdateExecutor extends AbstractDataProcessor {

  @Autowired
  private MetaDataService metadataService;

  @Override
  protected TaskResult execute(MetaDataWrapper metadataWrapper, Map options)
      throws CronJobExecutionException {
    String atlasLastModifiedTime = metadataWrapper.getAtlasMetadata().getLastModifiedTime();
    List<MetaData> metadatas = metadataService.findAll();

    for (MetaData metadata : metadatas) {
      String currLastModifiedTime =
          new MetaDataWrapper(metadata).getAtlasMetadata().getLastModifiedTime();
      if (currLastModifiedTime != null && currLastModifiedTime.equals(atlasLastModifiedTime)) {
        metadataService.delete(metadata);
        break;
      }
    }

    MetaData newMedata = metadataService.create(metadataWrapper.getMetaData());
    if (newMedata == null) {
      return TaskResult.FAILURE;
    }
    return TaskResult.SUCCESS;
  }

}
