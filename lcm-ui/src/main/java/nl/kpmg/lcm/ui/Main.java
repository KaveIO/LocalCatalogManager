package nl.kpmg.lcm.ui;


import nl.kpmg.lcm.server.ServerException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.kpmg.lcm.RedirectServer;
import nl.kpmg.lcm.InvalidArgumentsException;

import org.apache.commons.lang.ArrayUtils;

/**
 * Main class.
 *
 */
public class Main {

  private static final Logger LOG = Logger.getLogger(Main.class.getName());

  /**
   * Main method.
   *
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    try {
      LOG.log(Level.INFO, "Starting LCM UI");
      final UI ui = new UI();
      ui.start();

      LOG.log(Level.INFO, "Hit enter to stop it...");
      System.in.read();
      ui.stop();
    } catch (ServerException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
