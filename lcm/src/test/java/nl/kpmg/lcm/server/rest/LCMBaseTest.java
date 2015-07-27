package nl.kpmg.lcm.server.rest;

import java.io.File;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import nl.kpmg.lcm.server.Server;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/application-context.xml","/application-context-file.xml"})
public class LCMBaseTest {
    private static final String TEST_STORAGE_PATH = "test";

    protected Server server;
    protected WebTarget target;

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
    }

    @Before
    public void setUp() throws Exception {
        // start the server
        server = new Server();
        server.start();

        // create the client
        target = ClientBuilder.newClient().target(server.getBaseUri());
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }
    
}