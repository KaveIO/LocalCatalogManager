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

import nl.kpmg.lcm.common.data.FetchEndpoint;
import nl.kpmg.lcm.server.data.dao.FetchEndpointDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author S. Koulouzis
 */
@Service
public class FetchEndpointService {

  @Autowired
  private FetchEndpointDao dao;

  public List<FetchEndpoint> findAll() {
    return Lists.newLinkedList(dao.findAll());
  }

  public FetchEndpoint findOneById(String id) {
    return dao.findOne(id);
  }

  public FetchEndpoint create(FetchEndpoint fe) {
    return dao.save(fe);
  }

  public FetchEndpoint update(FetchEndpoint fe) {
    return dao.save(fe);
  }

  public void delete(FetchEndpoint fe) {
    dao.delete(fe);
  }
}
