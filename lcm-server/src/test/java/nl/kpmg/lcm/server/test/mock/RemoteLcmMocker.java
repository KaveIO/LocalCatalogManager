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

import nl.kpmg.lcm.common.data.RemoteLcm;

import org.bson.types.ObjectId;

/**
 *
 * @author shristov
 */
public class RemoteLcmMocker {
  public static RemoteLcm createRemoteLcm() {
    RemoteLcm remoteLcm = new RemoteLcm();
    remoteLcm.setId((new ObjectId()).toString());
    remoteLcm.setName("test");
    remoteLcm.setProtocol("https");
    remoteLcm.setDomain("0.0.0.0");
    remoteLcm.setPort(4444);
    remoteLcm.setUniqueId("lcmId");
    remoteLcm.setApplicationId("applicationId");
    remoteLcm.setApplicationKey("applicationKey");
    remoteLcm.setAlias("test-lcm-" + System.currentTimeMillis()); // making it unique to avoid duplication

    return remoteLcm;
  }
}
