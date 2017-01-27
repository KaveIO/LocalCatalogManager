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

package nl.kpmg.lcm.server.data.service;

import jersey.repackaged.com.google.common.collect.Lists;

import nl.kpmg.lcm.server.data.meatadata.MetaData;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author mhoekstra
 */
@Service
public class MetaDataService {

  @Autowired
  private MetaDataDao metaDataDao;

  public List<MetaData> findAll() {
    return Lists.newLinkedList(metaDataDao.findAll());
  }

  public MetaData findById(String id) {
    return metaDataDao.findOne(id);
  }

  public MetaDataDao getMetaDataDao() {
    return metaDataDao;
  }

  public void update(String metaDataId, MetaData metadata) {
    metadata.setId(metaDataId);
    metaDataDao.save(metadata);
  }
}
