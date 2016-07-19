package nl.kpmg.lcm;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.kpmg.lcm.server.Server;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.ui.UI;

import org.apache.commons.lang.ArrayUtils;

/**
 * Main class.
 *
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "https://localhost:8080/";

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
                LOG.log(Level.INFO, "Starting LCM server");
                final AbstractRedirectServer server = new nl.kpmg.lcm.server.RedirectServer();
                server.start();
                final Server secureServer = new Server();
                secureServer.start();

                LOG.log(Level.INFO, "Hit enter to stop it...");
                System.in.read();
                server.stop();
                secureServer.stop();
            } else if (command.equals("client")) {
                LOG.log(Level.INFO, "Client not implemented yet.");
            } else if (command.equals("ui")) {
                LOG.log(Level.INFO, "Starting LCM UI");
                final AbstractRedirectServer server = new nl.kpmg.lcm.ui.RedirectServer();
                server.start();
                final UI ui = new UI();
                ui.start();
                
                LOG.log(Level.INFO, "Hit enter to stop it...");
                System.in.read();
                ui.stop();
            } else if (command.equals("cli")) {
                LOG.log(Level.INFO, "Cli not implemented yet.");
            } else if (command.equals("help")) {
                if (arguments.length == 1) {
                    displayHelp(arguments[0]);
                } else {
                    displayHelp();
                }
            } else {
                throw new InvalidArgumentsException(String.format("Caught a unhandled command: %s", command));
            }
        }
        catch (InvalidArgumentsException e) {
            displayHelp(e);
        }
        catch (ServerException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Failed starting the server", ex);
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
