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

import jersey.repackaged.com.google.common.collect.Lists;

import nl.kpmg.lcm.common.data.LcmId;
import nl.kpmg.lcm.server.LcmIdGenerator;
import nl.kpmg.lcm.server.data.dao.LcmIdDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author shristov
 */
@Service
public class LcmIdService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LcmIdService.class.getName());

  private LcmIdGenerator generator = new LcmIdGenerator();

  @Autowired
  private LcmIdDao lcmIdDao;


  public LcmId getLcmIdObject() {
    if (lcmIdDao.count() <= 0) {
      return null;
    }

    return Lists.newLinkedList(lcmIdDao.findAll()).get(0);
  }

  public void create(LcmId lcmId) {
    lcmIdDao.save(lcmId);
  }

  public void create() {
    if (lcmIdDao.count() == 0) {
      LcmId lcmId = new LcmId();
      lcmId.setLcmId(generator.generateLcmId());
      lcmIdDao.save(lcmId);
    }
    LOGGER.info("The LCM id is : " + getLcmIdObject().getLcmId());
  }

  public void update(LcmId lcmId) {
    lcmIdDao.save(lcmId);
  }

  public void delete() {
    lcmIdDao.deleteAll();
  }
}
