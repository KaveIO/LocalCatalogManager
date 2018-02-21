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

import nl.kpmg.lcm.common.data.AuthorizedLcm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author shristov
 */
public class MongoAuthorizedLcmReadConverter implements Converter<DBObject, AuthorizedLcm> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MongoAuthorizedLcmReadConverter.class
      .getName());

  @Override
  public AuthorizedLcm convert(DBObject source) {
    AuthorizedLcm authorizedLcm =  new AuthorizedLcm();
    authorizedLcm.setId(source.get("_id").toString());
    authorizedLcm.setName((String) source.get("name"));
    authorizedLcm.setUniqueId((String) source.get("uniqueId"));
    authorizedLcm.setApplicationId((String) source.get("application-id"));
    authorizedLcm.setApplicationKey((String) source.get("application-key"));
    if(source.get("import-of-users-allowed") == null ) {
        authorizedLcm.setImportOfUsersAllowed(false);
    } else {
        authorizedLcm.setImportOfUsersAllowed((Boolean) source.get("import-of-users-allowed"));
    }

    return authorizedLcm;
  }
}
