/*
 * Copyright 2016 KPMG N.V. (unless otherwise stated).
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

import nl.kpmg.lcm.common.data.AuthorizedLcm;
import nl.kpmg.lcm.server.data.dao.AuthorizedLcmDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author S. Koulouzis
 */
@Service
public class AuthorizedLcmService {
  private static final Logger logger = LoggerFactory.getLogger(AuthorizedLcmService.class.getName());

  @Autowired
  private AuthorizedLcmDao dao;

  public List<AuthorizedLcm> findAll() {
    return Lists.newLinkedList(dao.findAll());
  }

  public AuthorizedLcm findOneById(String id) {
    return dao.findOne(id);
  }

  /**
   *
   * @param authorizedLcm :  valid AuthorizedLcm class. all the fields are mandatory
   * @return saved item - it will contains an valid id.
   */
  public AuthorizedLcm create(AuthorizedLcm authorizedLcm) {
    return dao.save(authorizedLcm);
  }

  /**
   *
   * @param authorizedLcm : all the sections are mandatory  except the application key.
   * In case applicationKey is not set the old one is used. This is implemented in security reasons.
   * It is not need to expose outside the module  the application key(it must be secret).
   * @return saved record.
   */
  public AuthorizedLcm update(AuthorizedLcm authorizedLcm) {
    if(authorizedLcm.getApplicationKey() ==  null) {
        AuthorizedLcm oldRecord =  dao.findOne(authorizedLcm.getId());
        authorizedLcm.setApplicationKey(oldRecord.getApplicationKey());
    }
    return dao.save(authorizedLcm);
  }

  public void delete(String  authorizedLcmId) {
      dao.delete(authorizedLcmId);
  }
}