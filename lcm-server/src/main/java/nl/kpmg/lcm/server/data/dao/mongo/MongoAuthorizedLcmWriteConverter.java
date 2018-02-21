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

import nl.kpmg.lcm.common.data.AuthorizedLcm;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author shristov
 */
public class MongoAuthorizedLcmWriteConverter implements Converter<AuthorizedLcm, DBObject> {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(MongoAuthorizedLcmWriteConverter.class.getName());

  @Override
  public DBObject convert(AuthorizedLcm source) {
    DBObject dbo = new BasicDBObject();
    if (source.getId() != null) {
      dbo.put("_id", new ObjectId(source.getId()));
    }
    dbo.put("name", source.getName());
    dbo.put("uniqueId", source.getUniqueId());
    dbo.put("application-id", source.getApplicationId());
    dbo.put("application-key", source.getApplicationKey());
    dbo.put("import-of-users-allowed", source.isImportOfUsersAllowed());

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
