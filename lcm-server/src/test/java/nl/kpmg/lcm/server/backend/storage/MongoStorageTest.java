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

package nl.kpmg.lcm.server.backend.storage;

import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.exception.LcmValidationException;
import nl.kpmg.lcm.server.test.mock.StorageMocker;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class MongoStorageTest {
   private String storageName = "mongo-storage";
   private String username =  "mongo";
   private String password = "mongo";
   private String databaseName = "lcm";
   private String hostname = "localhost";
   private String port = "12345";

  @Test
  public void testCreateStorage() {
    Storage correctStorage = StorageMocker.createMongoStorage();
    MongoStorage mongoStorage = new MongoStorage(correctStorage);
    Assert.assertNotNull(mongoStorage);
  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingUsername() {
    Storage incorrectStorage = new Storage();
    incorrectStorage.setName(storageName);
    Map options = new HashMap();
    options.put("password", password);
    options.put("database", databaseName);
    options.put("hostname",  hostname);
    options.put("port", port);
    incorrectStorage.setOptions(options);

    new MongoStorage(incorrectStorage);
  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingPassword() {
    Storage incorrectStorage = new Storage();
    incorrectStorage.setName(storageName);
    Map options = new HashMap();
    options.put("username", username);
    options.put("database", databaseName);
    options.put("hostname",  hostname);
    options.put("port", port);
    incorrectStorage.setOptions(options);

    new MongoStorage(incorrectStorage);

  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingDatabase() {
    Storage incorrectStorage = new Storage();
    incorrectStorage.setName(storageName);
    Map options = new HashMap();
    options.put("username", username);
    options.put("password", password);
    options.put("hostname",  hostname);
    options.put("port", port);
    incorrectStorage.setOptions(options);

    new MongoStorage(incorrectStorage);

  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingPort() {
    Storage incorrectStorage = new Storage();
    incorrectStorage.setName(storageName);
    Map options = new HashMap();
    options.put("username", username);
    options.put("password", password);
    options.put("database", databaseName);
    options.put("hostname",  hostname);
    incorrectStorage.setOptions(options);

    new MongoStorage(incorrectStorage);
  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingHostname() {
    Storage incorrectStorage = new Storage();
    incorrectStorage.setName(storageName);
    Map options = new HashMap();
    options.put("storagePath", "blahblah");
    incorrectStorage.setOptions(options);

    new MongoStorage(incorrectStorage);
  }
}