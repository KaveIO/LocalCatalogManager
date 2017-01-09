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

import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.rest.authentication.UserPasswordHashException;

import org.springframework.core.convert.converter.Converter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Specific Read Converter for User objects.
 *
 * This class converts DBObjects to User Objects at the moment these are loaded from the database.
 * All passwords which are loaded from the database are hashed and should be marked as such.
 *
 * @author mhoekstra
 */
public class MongoUserReadConverter implements Converter<DBObject, User> {

  @Override
  public User convert(DBObject source) {
    try {
      User user = new User();
      user.setId(source.get("_id").toString());
      user.setName((String) source.get("name"));
      user.setPassword((String) source.get("password"), false);
      user.setRole((String) source.get("role"));
      user.setHashed(true);
      return user;
    } catch (UserPasswordHashException ex) {
      Logger.getLogger(MongoUserReadConverter.class.getName()).log(Level.SEVERE,
          "Couldn't load user", ex);
      return null;
    }
  }
}
