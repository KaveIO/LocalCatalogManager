package nl.kpmg.lcm.server.metadata.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import nl.kpmg.lcm.server.data.service.EncryptDecryptService;
import nl.kpmg.lcm.server.rest.LCMBaseTest;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
* JUnit test class for encrypt and decrypt password string using JASYPT. 
* @author venkateswarlub
*/
public class EncryptDecryptServiceTest extends LCMBaseTest{
	
	@Autowired
	private StandardPBEStringEncryptor encryption;
	
	@Autowired
	private EncryptDecryptService encryptDecryptService;

	
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
