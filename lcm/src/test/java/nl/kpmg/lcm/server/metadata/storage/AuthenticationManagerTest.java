package nl.kpmg.lcm.server.metadata.storage;

import static org.junit.Assert.assertEquals;

import java.io.File;

import nl.kpmg.lcm.server.AuthenticationManager;
import nl.kpmg.lcm.server.data.service.EncryptDecryptService;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author venkateswarlub
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/application-context.xml","/application-context-file.xml"})
public class AuthenticationManagerTest {
	private static final String TEST_STORAGE_PATH = "test";
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private EncryptDecryptService encryptDecryptService;

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
    }
    
    @Test
    public void testServiceKeyValid(){
    	String servicekey = "ABC123";
    	authenticationManager.getAuthentication("admin","admin", servicekey);
    	assertEquals(true,authenticationManager.isAuthenticated());
    }
    
    @Test
    public void testIsAuthenticatedFalse(){    	   
    	assertEquals(false,authenticationManager.isAuthenticated());
    }
}
