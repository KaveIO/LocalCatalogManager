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

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class HiveStorageTest {
   private String storageName = "hive-storage";
   private String username =  "hive";
   private String password = "hive";
   private String databaseName = "default";
   private String url = "jdbc:hive2://192.168.1.1:10000";
   private String driver = "org.apache.hive.jdbc.HiveDriver";

  @Test
  public void testCreateStorage() {
    Storage correctStorage = new Storage();
    correctStorage.setName(storageName);
    Map options = new HashMap();
    options.put("username", username);
    options.put("password", password);
    options.put("database", databaseName);
    options.put("url",  url);
    options.put("driver", driver);
    correctStorage.setOptions(options);

    HiveStorage hiveStorage = new HiveStorage(correctStorage);
    Assert.assertNotNull(hiveStorage);
  }


  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingUsername() {
    Storage incorrectStorage = new Storage();
    incorrectStorage.setName(storageName);
    Map options = new HashMap();
    options.put("password", password);
    options.put("database", databaseName);
    options.put("url",  url);
    options.put("driver", driver);
    incorrectStorage.setOptions(options);

    new HiveStorage(incorrectStorage);
  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingPassword() {
    Storage incorrectStorage = new Storage();
    incorrectStorage.setName(storageName);
    Map options = new HashMap();
    options.put("username", username);
    options.put("database", databaseName);
    options.put("url",  url);
    options.put("driver", driver);
    incorrectStorage.setOptions(options);

    new HiveStorage(incorrectStorage);
    
  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingDatabase() {
    Storage incorrectStorage = new Storage();
    incorrectStorage.setName(storageName);
    Map options = new HashMap();
    options.put("username", username);
    options.put("password", password);
    options.put("url",  url);
    options.put("driver", driver);
    incorrectStorage.setOptions(options);

    new HiveStorage(incorrectStorage);

  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingDriver() {
    Storage incorrectStorage = new Storage();
    incorrectStorage.setName(storageName);
    Map options = new HashMap();
    options.put("username", username);
    options.put("password", password);
    options.put("database", databaseName);
    options.put("url",  url);
    incorrectStorage.setOptions(options);

    new HiveStorage(incorrectStorage);
  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingUrl() {
    Storage incorrectStorage = new Storage();
    incorrectStorage.setName(storageName);
    Map options = new HashMap();
    options.put("storagePath", "hive");
    incorrectStorage.setOptions(options);

    new HiveStorage(incorrectStorage);
  }
}
