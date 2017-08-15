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

import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.exception.LcmValidationException;
import nl.kpmg.lcm.server.test.mock.StorageMocker;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class HiveStorageTest {
   private String databaseName = "default";
   private String url = "jdbc:hive2://192.168.1.1:10000";
   private String driver = "org.apache.hive.jdbc.HiveDriver";

  @Test
  public void testCreateStorage() {
    Storage correctStorage = StorageMocker.createHiveStorage();
    HiveStorage hiveStorage = new HiveStorage(correctStorage);
    Assert.assertNotNull(hiveStorage);
  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingUsername() {
    Storage incorrectStorage = StorageMocker.createHiveStorage();
    Map options = new HashMap();
    options.put("url",  url);
    options.put("driver", driver);
    incorrectStorage.setOptions(options);

    new HiveStorage(incorrectStorage);
  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingPassword() {
    Storage incorrectStorage = StorageMocker.createHiveStorage();
    Map options = new HashMap();
    options.put("url",  url);
    options.put("driver", driver);
    incorrectStorage.setOptions(options);

    new HiveStorage(incorrectStorage);
    
  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingDatabase() {
    Storage incorrectStorage = StorageMocker.createHiveStorage();
    Map options = new HashMap();
    options.put("url",  url);
    options.put("driver", driver);
    incorrectStorage.setOptions(options);

    new HiveStorage(incorrectStorage);

  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingDriver() {
    Storage incorrectStorage = StorageMocker.createHiveStorage();
    Map options = new HashMap();
    options.put("database", databaseName);
    options.put("url",  url);
    incorrectStorage.setOptions(options);

    new HiveStorage(incorrectStorage);
  }

  @Test(expected = LcmValidationException.class)
  public void testValidateStorageMissingUrl() {
    Storage incorrectStorage = StorageMocker.createHiveStorage();
    Map options = new HashMap();
    options.put("storagePath", "hive");
    incorrectStorage.setOptions(options);

    new HiveStorage(incorrectStorage);
  }
}
