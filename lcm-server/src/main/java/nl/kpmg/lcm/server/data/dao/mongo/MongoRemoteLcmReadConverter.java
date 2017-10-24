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
import nl.kpmg.lcm.server.security.EncryptionException;
import nl.kpmg.lcm.server.security.SecurityEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;

import java.util.Map;

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

    String applicationKeyString = "application-key";
    Map applicationKeyMap = (Map) source.get(applicationKeyString);
    if (applicationKeyMap != null) {
      String encrypted = (String) applicationKeyMap.get("value");
      if (encrypted == null) {
        String message =
            "Error! Unable to read storage. The" + applicationKeyString + "value is null!";
        throw new IllegalStateException(message);
      }

      String initVector = (String) applicationKeyMap.get("init-vector");
      if (initVector == null) {
        String message =
            "Error! Unable to read storage. The" + applicationKeyString + "init vector is null!";
        throw new IllegalStateException(message);
      }
      SecurityEngine security = new SecurityEngine(initVector);
      try {
        String decrypted = security.decrypt(securityKey, encrypted);
        remoteLcm.setApplicationKey(decrypted);
      } catch (EncryptionException ex) {
        String message = "Error! Unable to read storage. The decryption failed!";
        throw new IllegalStateException(message);
      }
    }

    return remoteLcm;
  }

}
