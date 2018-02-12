/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.kpmg.lcm.server.cron.job.processor;

import nl.kpmg.lcm.common.data.TaskType;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;
import nl.kpmg.lcm.server.cron.TaskResult;
import nl.kpmg.lcm.server.cron.exception.CronJobExecutionException;
import nl.kpmg.lcm.server.cron.job.AbstractDataProcessor;
import nl.kpmg.lcm.server.data.service.MetaDataService;
import nl.kpmg.lcm.server.integration.atlas.TransformationException;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 *
 * @author shristov
 */
public class AtlasDataExecutor extends AbstractDataProcessor {
  private final org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(AtlasDataExecutor.class.getName());

  @Autowired
  private MetaDataService metadataService;

  @Override
  protected TaskResult execute(MetaDataWrapper metadataWrapper, Map options)
      throws CronJobExecutionException {
    TaskType taskType = TaskType.valueOf( (String) options.get("type"));

    if (taskType.equals(TaskType.ATLAS_INSERT)) {
      MetaData newMetadata = metadataService.create(metadataWrapper.getMetaData());
      if (newMetadata == null) {
        return TaskResult.FAILURE;
      }

    } else {
      // In both remaining cases - update and deletion, the old metadata is deleted.
      // When updating atlas metadata in the LCM, it is firstly deleted and then a new one
      // is created.
      metadataService.delete(metadataWrapper.getMetaData());

      if (taskType.equals(TaskType.ATLAS_UPDATE)) {
        try {
          MetaData updatedMetadata =
              atlasMetadataService.getOne(metadataWrapper.getAtlasMetadata().getGuid());
          MetaData newMedata = metadataService.create(updatedMetadata);
          if (newMedata == null) {
            return TaskResult.FAILURE;
          }
        } catch (TransformationException ex) {
          LOGGER.warn(ex.getMessage());
          return TaskResult.FAILURE;
        }
      }
    }
    return TaskResult.SUCCESS;
  }

}
