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

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

/**
 *
 * @author anaskar
 */
public class DatabaseInitialiser {

  public static final String MONGO_HOST = "localhost";
  public static final int MONGO_PORT = 27018;

  private static final MongodStarter starter = MongodStarter.getDefaultInstance();

  private MongodExecutable _mongodExe;
  private MongodProcess _mongod;

  private MongoClient _mongo;


  public void start() throws Exception {
    MongoCmdOptionsBuilder cmdBuilder = new MongoCmdOptionsBuilder();
    cmdBuilder.enableAuth(false);

    _mongodExe =
        starter.prepare(new MongodConfigBuilder().version(Version.Main.PRODUCTION)
            .cmdOptions(cmdBuilder.build())
            .net(new Net(MONGO_HOST, MONGO_PORT, Network.localhostIsIPv6())).build());
    _mongod = _mongodExe.start();


    _mongo = new MongoClient(MONGO_HOST, MONGO_PORT);

  }

  public void stop() {
    _mongod.stop();
    _mongodExe.stop();

  }
}
