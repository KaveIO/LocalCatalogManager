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

import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.Storage;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shristov
 */
public class StorageMocker {
  private static final String STORAGE_PATH = System.getProperty("java.io.tmpdir");
  private static final String CSV_STORAGE_NAME = "csv-storage";
  private static final String FILE_STORAGE_NAME = "file-storage";

  public static Storage createFileStorage() {
    Storage fileStorage = new Storage();
    fileStorage.setName(FILE_STORAGE_NAME);
    Map options = new HashMap();
    options.put("storagePath", STORAGE_PATH);
    fileStorage.setOptions(options);
    fileStorage.setType(DataFormat.FILE);
    return fileStorage;
  }

  public static Storage createCsvStorage() {
    Storage csvStorage = new Storage();
    csvStorage.setName(CSV_STORAGE_NAME);
    Map options = new HashMap();
    options.put("storagePath", STORAGE_PATH);
    csvStorage.setOptions(options);
    csvStorage.setType(DataFormat.CSV);
    return csvStorage;
  }

  public static Storage createHiveStorage() {
    Storage backendStorage = new Storage();
    backendStorage.setName("hive-sotrage");
    Map options = new HashMap();
    options.put("database", "default");
    options.put("url", "jdbc:hive2://10.191.30.201:10000");
    options.put("driver", "org.apache.hive.jdbc.HiveDriver");
    backendStorage.setOptions(options);
    Map credentials = new HashMap();
    credentials.put("username", "hive");
    credentials.put("password", "hive");
    backendStorage.setCredentials(credentials);
    backendStorage.setType(DataFormat.HIVE);

    return backendStorage;
  }

  public static Storage createMongoStorage() {
    Storage backendStorage = new Storage();
    backendStorage.setName("mongo-sotrage");
    Map options = new HashMap();
    options.put("database", "lcm");
    options.put("hostname", "localhost");
    options.put("port", "12345");
    backendStorage.setOptions(options);
    Map credentials = new HashMap();
    credentials.put("username", "mongo");
    credentials.put("password", "mongo");
    backendStorage.setCredentials(credentials);
    backendStorage.setType(DataFormat.MONGO);

    return backendStorage;
  }
}
