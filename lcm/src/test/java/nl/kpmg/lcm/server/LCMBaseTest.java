package nl.kpmg.lcm.server;

import java.io.File;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import nl.kpmg.lcm.server.Server;
import nl.kpmg.lcm.server.ServerException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/application-context.xml","/application-context-file.xml"})
@Ignore
public class LCMBaseTest {
    private static final String TEST_STORAGE_PATH = "test";

    protected static Server server;
    protected static WebTarget target;

    @BeforeClass
    public static void setUpClass() {
        File file;
        file = new File(TEST_STORAGE_PATH);
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/metadata");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/taskdescription");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/taskschedule");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/backend");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/users");
        file.mkdir();
        file = new File(TEST_STORAGE_PATH + "/userGroups");
        file.mkdir();
        // start the server
        try {
			server = new Server();
			server.start();
		} catch (ServerException e) {
			
			e.printStackTrace();
		}        
       

        // create the client
        target = ClientBuilder.newClient().target(server.getBaseUri());

    }

    @AfterClass
    public static void tearDownClass() {
        File file;
        file = new File(TEST_STORAGE_PATH);
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/metadata");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/taskdescription");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/taskschedule");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/backend");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/users");
        file.delete();
        file = new File(TEST_STORAGE_PATH + "/userGroups");
        file.delete();
        server.stop();
    }
       
}