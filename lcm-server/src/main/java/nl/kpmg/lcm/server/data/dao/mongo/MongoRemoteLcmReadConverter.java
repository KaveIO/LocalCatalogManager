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
package nl.kpmg.lcm.server.data.dao.mongo;

import com.mongodb.DBObject;

import nl.kpmg.lcm.common.data.RemoteLcm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author shristov
 */
public class MongoRemoteLcmReadConverter implements Converter<DBObject, RemoteLcm> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MongoRemoteLcmReadConverter.class
      .getName());
  @Value("${lcm.server.security.encryption.key}")
  private String securityKey;

  @Override
  public RemoteLcm convert(DBObject source) {
    RemoteLcm remoteLcm = new RemoteLcm();
    remoteLcm.setId(source.get("_id").toString());
    remoteLcm.setName((String) source.get("name"));
    remoteLcm.setDomain((String) source.get("domain"));
    remoteLcm.setProtocol((String) source.get("protocol"));
    remoteLcm.setPort((Integer) source.get("port"));
    remoteLcm.setStatus((String) source.get("status"));
    remoteLcm.setUniqueId((String) source.get("unique-lcm-id"));
    remoteLcm.setApplicationId((String) source.get("application-id"));
    remoteLcm.setApplicationKey((String) source.get("application-key"));

    return remoteLcm;
  }

}