package nl.kpmg.lcm.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
      final Server server = new Server();
      server.start();

      LOG.log(Level.INFO, "Hit enter to stop it...");
      System.in.read();
      server.stop();
    } catch (ServerException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
