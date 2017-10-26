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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import nl.kpmg.lcm.common.data.RemoteLcm;
import nl.kpmg.lcm.common.exception.LcmException;
import nl.kpmg.lcm.common.rest.authentication.PasswordHash;
import nl.kpmg.lcm.common.rest.authentication.UserPasswordHashException;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author shristov
 */
public class MongoRemoteLcmWriteConverter implements Converter<RemoteLcm, DBObject> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MongoRemoteLcmWriteConverter.class
      .getName());
  @Value("${lcm.server.security.encryption.key}")
  private String securityKey;

  @Override
  public DBObject convert(RemoteLcm source) {
    DBObject dbo = new BasicDBObject();
    if (source.getId() != null) {
      dbo.put("_id", new ObjectId(source.getId()));
    }
    dbo.put("name", source.getName());
    dbo.put("domain", source.getDomain());
    dbo.put("protocol", source.getProtocol());
    dbo.put("port", source.getPort());
    dbo.put("status", source.getStatus());
    dbo.put("unique-lcm-id", source.getUniqueId());
    dbo.put("application-id", source.getApplicationId());
    String applicationKey = source.getApplicationKey();

    if (applicationKey == null) {
      String message = "Error! Unable to write remote lcm. The application-key is null!";
      throw new IllegalStateException(message);
    }

    try {
      String key = PasswordHash.createHash(applicationKey);
      dbo.put("application-key", key);
    } catch (UserPasswordHashException ex) {
      String message =
          "Unable to write RemoteLcm  in Mongo! Authorized LCM  name:" + source.getName();
      LOGGER.error(message);
      throw new LcmException(message);
    }

    return dbo;
  }

  protected String generateInitVector() {
    String initVector = String.valueOf(System.currentTimeMillis());
    int neededChars = 16 - initVector.length();
    Object object = new Object();
    if (object.toString().length() > neededChars) {
      int endIndex = object.toString().length();
      int startIndex = endIndex - neededChars;
      initVector += object.toString().substring(startIndex, endIndex);
    } else {
      initVector += String.valueOf(System.currentTimeMillis());
      initVector = initVector.substring(0, 16);
    }
    return initVector;
  }

}
