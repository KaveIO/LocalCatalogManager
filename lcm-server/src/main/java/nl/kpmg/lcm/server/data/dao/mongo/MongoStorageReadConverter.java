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

import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.server.backend.storage.AzureStorage;
import nl.kpmg.lcm.server.backend.storage.HiveStorage;
import nl.kpmg.lcm.server.backend.storage.MongoStorage;
import nl.kpmg.lcm.server.backend.storage.S3FileStorage;
import nl.kpmg.lcm.server.security.EncryptionException;
import nl.kpmg.lcm.server.security.SecurityEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author shristov
 */
public class MongoStorageReadConverter implements Converter<DBObject, Storage> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MongoStorageReadConverter.class
      .getName());
  @Value("${lcm.server.security.encryption.key}")
  private String securityKey;

  @Override
  public Storage convert(DBObject source) {
    Storage storage = new Storage();
    storage.setId(source.get("_id").toString());
    storage.setName((String) source.get("name"));
    storage.setStatus((String) source.get("status"));
    storage.setType((String) source.get("type"));
    Map sourceMap = source.toMap();
    Map enrichmentPropertiesMap = (Map) sourceMap.get("enrichment-properties");
    if (enrichmentPropertiesMap != null) {
      storage.setEnrichmentProperties(enrichmentPropertiesMap);
    }
    Map optionsMap = (Map) sourceMap.get("options");
    if (optionsMap != null) {
      storage.setOptions(optionsMap);
    }

    Map credentialsMap = (Map) sourceMap.get("credentials");
    if (credentialsMap != null) {
      storage = mapStorage(storage, credentialsMap);
    }

    return storage;
  }

  private Storage mapStorage(Storage storage, Map credentials) {
    Set<String> encryptedFields = null;
    if (HiveStorage.getSupportedStorageTypes().contains(storage.getType())) {
      encryptedFields = HiveStorage.getEncryptedCredentialsFields();
    } else if (MongoStorage.getSupportedStorageTypes().contains(storage.getType())) {
      encryptedFields = MongoStorage.getEncryptedCredentialsFields();
    } else if (S3FileStorage.getSupportedStorageTypes().contains(storage.getType())) {
      encryptedFields = S3FileStorage.getEncryptedCredentialsFields();
    }  else if (AzureStorage.getSupportedStorageTypes().contains(storage.getType())) {
      encryptedFields = AzureStorage.getEncryptedCredentialsFields();
    }

    if (encryptedFields == null) {
      encryptedFields = new HashSet();
    }

    for (String field : encryptedFields) {
      Map fieldMap = (Map) credentials.get(field);
      if (fieldMap == null) {
        String message = "Error! Unable to read storage. The " + field + " section is null!";
        throw new IllegalStateException(message);
      }
      String encrypted = (String) fieldMap.get("value");
      if (encrypted == null) {
        String message = "Error! Unable to read storage. The " + field + "value is null!";
        throw new IllegalStateException(message);
      }

      String initVector = (String) fieldMap.get("init-vector");
      if (initVector == null) {
        String message = "Error! Unable to read storage. The " + field + " init vector is null!";
        throw new IllegalStateException(message);
      }
      SecurityEngine security = new SecurityEngine(initVector);
      try {
        String decrypted = security.decrypt(securityKey, encrypted);
        credentials.put(field, decrypted);
      } catch (EncryptionException ex) {
        String message = "Error! Unable to read storage. The decryption failed!";
        throw new IllegalStateException(message);
      }
    }

    storage.setCredentials(credentials);
    return storage;
  }
}
