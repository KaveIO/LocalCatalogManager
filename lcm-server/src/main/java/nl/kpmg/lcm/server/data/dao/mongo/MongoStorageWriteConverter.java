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

import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.server.backend.storage.AzureStorage;
import nl.kpmg.lcm.server.backend.storage.HiveStorage;
import nl.kpmg.lcm.server.backend.storage.MongoStorage;
import nl.kpmg.lcm.server.backend.storage.S3FileStorage;
import nl.kpmg.lcm.server.security.EncryptionException;
import nl.kpmg.lcm.server.security.SecurityEngine;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author shristov
 */
public class MongoStorageWriteConverter implements Converter<Storage, DBObject> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MongoStorageWriteConverter.class
      .getName());
  @Value("${lcm.server.security.encryption.key}")
  private String securityKey;

  @Override
  public DBObject convert(Storage source) {
    DBObject dbo = new BasicDBObject();
    if (source.getId() != null) {
      dbo.put("_id", new ObjectId(source.getId()));
    }
    dbo.put("name", source.getName());
    dbo.put("type", source.getType());
    if (source.getEnrichmentProperties() != null) {
      dbo.put("enrichment-properties", source.getEnrichmentProperties());
    }

    if (source.getOptions() != null) {
      dbo.put("options", source.getOptions());
    }

    if (source.getStatus()!= null) {
      dbo.put("status", source.getStatus());
    }

    Map credentialsMap = source.getCredentials();
    if (credentialsMap != null) {
      credentialsMap = mapStorage(credentialsMap, source);
      dbo.put("credentials", credentialsMap);
    }

    return dbo;
  }

  private Map mapStorage(Map credentials, Storage storage) {
    Set<String> encryptedFields = null;
    if (HiveStorage.getSupportedStorageTypes().contains(storage.getType())) {
      encryptedFields = HiveStorage.getEncryptedCredentialsFields();
    } else if (MongoStorage.getSupportedStorageTypes().contains(storage.getType())) {
      encryptedFields = MongoStorage.getEncryptedCredentialsFields();
    } else if (S3FileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      encryptedFields = S3FileStorage.getEncryptedCredentialsFields();
    } else if (AzureStorage.getSupportedStorageTypes().contains(storage.getType())) {
      encryptedFields = AzureStorage.getEncryptedCredentialsFields();
    }

    if (encryptedFields == null) {
      encryptedFields = new HashSet();
    }

    for (String field : encryptedFields) {
      String fieldValue = (String) credentials.get(field);
      if (fieldValue == null) {
        String message = "Error! Unable to write storage. The " + field + " is null!";
        throw new IllegalStateException(message);
      }

      String initVector = generateInitVector();
      Map fieldMap = new HashMap();
      fieldMap.put("init-vector", initVector);

      try {
        SecurityEngine security = new SecurityEngine(initVector);
        String encryptedFieldValue = security.encrypt(securityKey, fieldValue);
        fieldMap.put("value", encryptedFieldValue);
      } catch (EncryptionException ex) {
        String message =
            "Error! Unable to write a storage. The encryption failed. Message: " + ex.getMessage();
        throw new IllegalStateException(message, ex);
      }

      credentials.put(field, fieldMap);
    }

    return credentials;
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
