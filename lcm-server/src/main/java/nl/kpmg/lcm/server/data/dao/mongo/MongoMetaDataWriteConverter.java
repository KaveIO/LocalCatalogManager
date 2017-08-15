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

import nl.kpmg.lcm.common.data.metadata.MetaData;

import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author mhoekstra
 */
public class MongoMetaDataWriteConverter implements Converter<MetaData, DBObject> {

  @Override
  public DBObject convert(MetaData source) {
    DBObject dbo = new BasicDBObject();
    if (source.getId() != null) {
      dbo.put("_id", new ObjectId(source.getId()));
    }
    putIfNotNull(dbo, "name", source.getName());
    dbo.putAll(source.anyGetter());
    return dbo;
  }

  private void putIfNotNull(DBObject dbo, String key, String value) {
    if (value != null) {
      dbo.put(key, value);
    }
  }
}
