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

package nl.kpmg.lcm.server.data.dao.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import nl.kpmg.lcm.common.data.User;
import nl.kpmg.lcm.common.rest.authentication.PasswordHash;
import nl.kpmg.lcm.common.rest.authentication.UserPasswordHashException;

import org.bson.types.ObjectId;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 * Specific Write Converter for User objects. This class converts User objects to DBObjects at the
 * moment these are persisted to the database. The operation here is a bit delicate since the
 * passwords in the database are hashed.
 *
 * @author mhoekstra
 */
public class MongoUserWriteConverter implements Converter<User, DBObject> {

  @Override
  public DBObject convert(User source) {
    try {
      DBObject dbo = new BasicDBObject();
      if (source.getId() != null) {
        dbo.put("_id", new ObjectId(source.getId()));
      }
      dbo.put("name", source.getName());
      dbo.put("role", source.getRole());
      dbo.put("origin", source.getOrigin());
      if(source.getAllowedPathList() !=  null  && source.getAllowedPathList().size() > 0) {
        dbo.put("pathList", source.getAllowedPathList());
      }

      if(source.getAllowedMetadataList() != null && source.getAllowedMetadataList().size() > 0)
      dbo.put("metadataList", source.getAllowedMetadataList());

      if (source.getNewPassword() != null) {
        dbo.put("password", PasswordHash.createHash(source.getNewPassword()));
      } else {
        if (source.isHashed()) {
          dbo.put("password", source.getPassword());
        } else  if(source.getPassword() != null){
          dbo.put("password", PasswordHash.createHash(source.getPassword()));
        }
      }

      return dbo;
    } catch (UserPasswordHashException ex) {
      LoggerFactory.getLogger(MongoUserWriteConverter.class.getName()).error(
          "Failure in hashing password while writing to DB", ex);
    }
    return null;
  }
}
