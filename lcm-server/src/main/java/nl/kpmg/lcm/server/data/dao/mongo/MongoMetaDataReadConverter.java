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

import com.mongodb.DBObject;

import nl.kpmg.lcm.server.data.MetaData;

import org.springframework.core.convert.converter.Converter;

import java.util.Set;

/**
 *
 * @author mhoekstra
 */
public class MongoMetaDataReadConverter implements Converter<DBObject, MetaData> {

  @Override
  public MetaData convert(DBObject source) {
    MetaData metaData = new MetaData();

    Set<String> keySet = source.keySet();
    for (String key : keySet) {
      switch (key) {
        case "_id":
          metaData.setId(source.get(key).toString());
          break;
        case "name":
          metaData.setName(source.get(key).toString());
          break;
        default:
          metaData.anySetter(key, source.get(key));
      }
    }
    return metaData;
  }
}
