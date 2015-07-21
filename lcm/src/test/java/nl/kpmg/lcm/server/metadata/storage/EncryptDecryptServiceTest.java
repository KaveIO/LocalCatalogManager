package nl.kpmg.lcm.server.metadata.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

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
* JUnit test class for encrypt and decrypt password string using JASYPT. 
* @author venkateswarlub
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/application-context.xml","/application-context-file.xml"})
public class EncryptDecryptServiceTest {
	private static final String TEST_STORAGE_PATH = "test";
	@Autowired
	private StandardPBEStringEncryptor encryption;
	
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
	public void testAutowiredInstance() {		
		assertNotNull(encryptDecryptService);
		assertNotNull(encryption);		
	}
	
	@Test
	public void testEncryptString() {		
		String encpwd = encryption.encrypt("admin");
		assertNotNull(encpwd);
		assertNotNull("admin",encpwd);
	}
	
	@Test
	public void testDecryptString() {		
		String encpwd = encryption.encrypt("admin");
		//"E8Dh/7yI1Nh6NcDGfyWpIw=="		
		assertEquals("admin",encryption.decrypt(encpwd));
	}
	
	@Test
	public void testEncryptString1() {
		
		String encpwd = encryptDecryptService.getEncryptDecryptService().encrypt("admin");
		assertNotNull(encpwd);
		assertNotNull("admin",encpwd);
	}
	
	@Test
	public void testDecryptString1() {		
		String encpwd = encryptDecryptService.getEncryptDecryptService().encrypt("admin");
		//"E8Dh/7yI1Nh6NcDGfyWpIw=="		
		assertEquals("admin",encryptDecryptService.getEncryptDecryptService().decrypt(encpwd));
	}
}
