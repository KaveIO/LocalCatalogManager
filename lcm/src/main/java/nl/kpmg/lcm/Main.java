package nl.kpmg.lcm;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.kpmg.lcm.server.Server;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main class.
 *
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/myapp/";

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
                // Load spring beans 
            	ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"application-context.xml","application-context-dao.xml"});
            	
            	final Server server = new Server(arguments);
                server.start();

                LOG.log(Level.INFO, "Hit enter to stop it...");
                System.in.read();
                
                server.stop();
            } else if (command.equals("client")) {
                LOG.log(Level.INFO, "Client not implemented yet.");
            } else if (command.equals("ui")) {
                LOG.log(Level.INFO, "Ui not implemented yet.");
            } else if (command.equals("help")) {
                if (arguments.length == 1) {
                    displayHelp(arguments[0]);
                } else {
                    displayHelp();
                }
            } else {
                throw new InvalidArgumentsException(String.format("Caught a unhandled command: %s", command));
            }
        } catch (InvalidArgumentsException e) {
            displayHelp(e);
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
