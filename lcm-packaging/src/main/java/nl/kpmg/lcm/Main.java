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
package nl.kpmg.lcm;

import nl.kpmg.lcm.common.InvalidArgumentsException;
import nl.kpmg.lcm.server.Server;
import nl.kpmg.lcm.common.ServerException;
import nl.kpmg.lcm.ui.UI;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main class.
 *
 */
public class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class.getName());

  /**
   * Main method.
   *
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    try {
      if (args.length == 0) {
        throw new InvalidArgumentsException("No command found");
      }

      final String command = args[0];
      final String[] arguments = (String[]) ArrayUtils.removeElement(args, command);

      if (command.equals("server")) {
        LOG.info( "Starting LCM server");

        final Server server = new Server();
        server.start();

        LOG.info( "Hit enter to stop it...");
        System.in.read();

        server.stop();
      } else if (command.equals("ui")) {
        LOG.info( "Starting LCM UI");

        final UI ui = new UI();
        ui.start();

        LOG.info( "Hit enter to stop it...");
        System.in.read();

        ui.stop();
      } else if (command.equals("cli")) {
        LOG.info( "Cli not implemented yet.");
      } else if (command.equals("help")) {
        if (arguments.length == 1) {
          displayHelp(arguments[0]);
        } else {
          displayHelp();
        }
      } else {
        throw new InvalidArgumentsException(
            String.format("Caught a unhandled command: %s", command));
      }
    } catch (InvalidArgumentsException e) {
      displayHelp(e);
    } catch (ServerException ex) {
      LOG.error( "Failed starting the server", ex);
    }
  }

  private static void displayHelp() {
    System.out.println("Help text");
  }

  private static void displayHelp(InvalidArgumentsException e) {
    System.out.println(e.getMessage());
    displayHelp();
  }

  private static void displayHelp(String command) {
    switch (command) {
      case "server":
        System.out.println("Specific server help text");
        break;
      case "client":
        System.out.println("Specific client help text");
        break;
      case "ui":
        System.out.println("Specific uit help text");
        break;
    }
  }
}
