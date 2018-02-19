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
package nl.kpmg.lcm.server.backend.storage;

import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.TestResult;
import nl.kpmg.lcm.server.data.FileSystemAdapter;
import nl.kpmg.lcm.server.data.LocalFileSystemAdapter;
import nl.kpmg.lcm.server.data.azure.AzureFileSystemAdapter;
import nl.kpmg.lcm.server.data.hdfs.HdfsFileSystemAdapter;
import nl.kpmg.lcm.server.data.s3.S3FileSystemAdapter;

import org.apache.metamodel.DataContextFactory;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author shristov
 */
public class StorageTester {
  public TestResult testAccessability(Storage storage) {
    try {
      if (DataFormat.CSV.equals(storage.getType()) || DataFormat.JSON.equals(storage.getType())
          || DataFormat.FILE.equals(storage.getType())) {

          LocalFileStorage fileStorage = new LocalFileStorage(storage);
          LocalFileSystemAdapter adapter = new LocalFileSystemAdapter(fileStorage);
          return adapter.testConnection();
      } else if (DataFormat.S3FILE.equals(storage.getType())) {

        S3FileStorage s3storage = new S3FileStorage(storage);
        FileSystemAdapter adapter = new S3FileSystemAdapter(s3storage);
        return adapter.testConnection();
      } else if (DataFormat.HDFSFILE.equals(storage.getType())) {

        HdfsFileStorage hdfsFileStorage = new HdfsFileStorage(storage);
        HdfsFileSystemAdapter adapter = new HdfsFileSystemAdapter(hdfsFileStorage);
        return adapter.testConnection();
      } else if (DataFormat.MONGO.equals(storage.getType())) {

        MongoStorage mongoStorage = new MongoStorage(storage);
        String password = mongoStorage.getPassword();
        DataContextFactory.createMongoDbDataContext(mongoStorage.getHostname(),
            Integer.parseInt(mongoStorage.getPort()), mongoStorage.getDatabase(),
            mongoStorage.getUsername(), password.toCharArray());
        return new TestResult("OK", TestResult.TestCode.ACCESIBLE);
      } else if (DataFormat.HIVE.equals(storage.getType())) {

        HiveStorage hiveStorage = new HiveStorage(storage);
        Class.forName(hiveStorage.getDriver());
        Connection connection =
            DriverManager.getConnection(hiveStorage.getUrl(), hiveStorage.getUsername(),
                hiveStorage.getPassword());
        return new TestResult("OK", TestResult.TestCode.ACCESIBLE);
      }  else if (DataFormat.AZUREFILE.equals(storage.getType()) ||
              DataFormat.AZURECSV.equals(storage.getType())) {

          AzureStorage fileStorage = new AzureStorage(storage);
          AzureFileSystemAdapter adapter = new AzureFileSystemAdapter(fileStorage);
          return adapter.testConnection();
      }
    } catch (Exception e) {
      return new TestResult(e.getMessage(), TestResult.TestCode.INACCESSIBLE);
    }

    return new TestResult("Something wrong happened.", TestResult.TestCode.INACCESSIBLE);
  }
}
