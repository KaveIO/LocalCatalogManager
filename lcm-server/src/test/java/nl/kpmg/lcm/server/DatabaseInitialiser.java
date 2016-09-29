/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
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

package nl.kpmg.lcm.server;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 *
 * @author anaskar
 */
public class DatabaseInitialiser {

  public static final String MONGO_HOST = "localhost";
  public static final int MONGO_PORT = 27017;

  public static final String LCM_DATABASE = "lcm";
  public static final String LCM_USER = "lcm";
  public static final String LCM_PASSWORD = "lcm";

  private MongoMockDatabase mockDatabase;

  public void start() throws Exception {
    try {
      mockDatabase = createMockDatabase(MONGO_HOST, MONGO_PORT);

      createMockUser(LCM_DATABASE, LCM_USER, LCM_PASSWORD);
    } catch (Exception ex) {
      stop();
      throw ex;
    }
  }

  public void stop() {
    if (mockDatabase != null) {
      if (mockDatabase.mongoClient != null) {
        mockDatabase.mongoClient.close();
      }
      if (mockDatabase.mongodProcess != null) {
        mockDatabase.mongodProcess.stop();
      }
      if (mockDatabase.mongodExecutable != null) {
        mockDatabase.mongodExecutable.stop();
      }
    }
  }

  private MongoMockDatabase createMockDatabase(String host, int port) throws IOException {
    MongodStarter dbStarter = MongodStarter.getDefaultInstance();
    MongodExecutable mongodExecutable = dbStarter.prepare(new MongodConfigBuilder()
        .version(Version.Main.V2_4).net(new Net(port, Network.localhostIsIPv6())).build());
    MongodProcess mongod = mongodExecutable.start();
    MongoClient mongoClient = new MongoClient(host, port);

    return new MongoMockDatabase(mongodExecutable, mongod, mongoClient);
  }

  private void loadMockData(List<String> mockFiles) throws IOException {
    for (String mockFile : mockFiles) {
      String[] split = mockFile.split("\\.");
      DB database = mockDatabase.mongoClient.getDB(split[0]);

      Path filePath = Paths.get(".", "src", "test", "resources", "mock", mockFile);
      BufferedReader jsonReader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
      StringBuilder json;
      for (json = new StringBuilder(); jsonReader.ready(); json.append(jsonReader.readLine())) {
      }

      BasicDBList mockData = (BasicDBList) JSON.parse(json.toString());
      WriteResult insert = database.createCollection(split[1], new BasicDBObject())
          .insert(mockData.toArray(new BasicDBObject[mockData.size()]), database.getWriteConcern());

      String error = insert.getError();
    }
  }

  private void createMockUser(String database, String user, String password) {
    DB db = mockDatabase.mongoClient.getDB(database);
    db.addUser(user, password.toCharArray());
  }

  private class MongoMockDatabase {

    public final MongodExecutable mongodExecutable;
    public final MongodProcess mongodProcess;
    public final MongoClient mongoClient;

    public MongoMockDatabase(MongodExecutable mongodExecutable, MongodProcess mongodProcess,
        MongoClient mongoClient) {
      this.mongodExecutable = mongodExecutable;
      this.mongodProcess = mongodProcess;
      this.mongoClient = mongoClient;
    }
  }
}
