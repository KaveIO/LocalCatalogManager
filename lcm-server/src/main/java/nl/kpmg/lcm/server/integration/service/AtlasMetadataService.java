/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.kpmg.lcm.server.integration.service;

import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.server.integration.atlas.AtlasMetadataTransformer;
import nl.kpmg.lcm.server.integration.atlas.AtlasRequestProxy;
import nl.kpmg.lcm.server.integration.atlas.TransformationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shristov
 */
@Service
public class AtlasMetadataService {

  @Autowired
  AtlasRequestProxy requestProxy;

  @Autowired
  AtlasMetadataTransformer metadataTransformator;

  private static final Logger LOGGER = LoggerFactory
      .getLogger(AtlasMetadataService.class.getName());

  private List<MetaData> getAll(String entityType) {
    Map<String, Object> result = requestProxy.searchEntitiesByType(entityType);
    List<MetaData> metadataList = new ArrayList();
    List<Object> entitiesList = (List) result.get("entities");
    for (Object entityObject : entitiesList) {

      Map entity = (Map) entityObject;
      String guid = (String) entity.get("guid");
      try {

        MetaData metadata = getOne(guid);
        if(metadata != null) {
            metadataList.add(metadata);
        }
      } catch (Exception e) {
        LOGGER.warn("Not able to get metadata with guid: " + guid);
      }
    }

    return metadataList;
  }


  public MetaData getOne(String guid) throws TransformationException {
    Map fullEntity = requestProxy.getEntity(guid);
    if(fullEntity == null){
        return null;
    }
    MetaData metadata = metadataTransformator.transform(fullEntity);

    return metadata;
  }

  public List<MetaData> getAll() {
    return getAll("hive_table");
  }
}
