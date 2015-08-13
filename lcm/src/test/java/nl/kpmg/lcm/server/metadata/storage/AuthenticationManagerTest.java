package nl.kpmg.lcm.server.metadata.storage;

import static org.junit.Assert.assertEquals;
import nl.kpmg.lcm.server.AuthenticationManager;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.service.EncryptDecryptService;
import nl.kpmg.lcm.server.rest.LCMBaseTest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author venkateswarlub
 *
 */
public class AuthenticationManagerTest extends LCMBaseTest {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private EncryptDecryptService encryptDecryptService;
	    
    @Test
    public void testServiceKeyValid() throws ServerException{
    	String servicekey = "ABC123";
    	authenticationManager.getAuthentication("admin","admin", servicekey);
    	assertEquals(true,authenticationManager.isAuthenticated());
    }
    
    @Test
    public void testIsAuthenticatedFalse(){    	   
    	assertEquals(false,authenticationManager.isAuthenticated());
    }
}
