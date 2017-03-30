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
package nl.kpmg.lcm.server.test.mock;

import nl.kpmg.lcm.server.data.Storage;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shristov
 */
public class StorageMocker {
  private static final String CSV_STORAGE_PATH = System.getProperty("java.io.tmpdir");
  private static final String CSV_STORAGE_NAME = "csv-storage";

  public static Storage createCsvStorage() {
    Storage csvStorage = new Storage();
    csvStorage.setName(CSV_STORAGE_NAME);
    Map options = new HashMap();
    options.put("storagePath", CSV_STORAGE_PATH);
    csvStorage.setOptions(options);
    csvStorage.setType("csv");
    return csvStorage;
  }

  public static Storage createHiveStorage() {
    Storage backendStorage = new Storage();
    backendStorage.setName("hive-sotrage");
    Map options = new HashMap();
    options.put("username", "hive");
    options.put("password", "hive");
    options.put("database", "default");
    options.put("url", "jdbc:hive2://10.191.30.201:10000");
    options.put("driver", "org.apache.hive.jdbc.HiveDriver");
    backendStorage.setOptions(options);
    backendStorage.setType("hive");

    return backendStorage;
  }
}
