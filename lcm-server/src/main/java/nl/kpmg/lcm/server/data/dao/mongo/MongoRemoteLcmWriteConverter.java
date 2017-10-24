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
import nl.kpmg.lcm.server.security.EncryptionException;
import nl.kpmg.lcm.server.security.SecurityEngine;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;

import java.util.HashMap;
import java.util.Map;

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
      String message = "Error! Unable to write storage. The application-key is null!";
      throw new IllegalStateException(message);
    }

    String initVector = generateInitVector();
    Map fieldMap = new HashMap();
    fieldMap.put("init-vector", initVector);

    try {
      SecurityEngine security = new SecurityEngine(initVector);
      String encryptedFieldValue = security.encrypt(securityKey, applicationKey);
      fieldMap.put("value", encryptedFieldValue);
    } catch (EncryptionException ex) {
      String message =
          "Error! Unable to write a storage. The encryption failed. Message: " + ex.getMessage();
      throw new IllegalStateException(message, ex);
    }
    dbo.put("application-key", fieldMap);


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
