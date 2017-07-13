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

package nl.kpmg.lcm.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main class.
 *
 */
public class Main {

  private static final Logger LOG = LoggerFactory.getLogger("sout");

  /**
   * Main method.
   *
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    try {
      LOG.info( "Starting LCM Server");
      final Server server = new Server();
      server.start();

      LOG.info( "Hit enter to stop it...");
      System.in.read();
      server.stop();
    } catch (ServerException ex) {
      LOG.error( "Error! Unable to the start server. Message: " + ex.getMessage());
    }
  }
}
