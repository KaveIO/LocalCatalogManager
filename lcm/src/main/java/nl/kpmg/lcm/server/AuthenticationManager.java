package nl.kpmg.lcm.server;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import nl.kpmg.lcm.server.data.service.EncryptDecryptService;

/**
 * Authentication Manager
 * @author venkateswarlub
 *
 */
public class AuthenticationManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationManager.class);
	private boolean auth = false;
	
	@Autowired
	private EncryptDecryptService encryptDecryptService;
	
	Map<String,String> userMap = new HashMap<String,String>();
	Map<String,String> servicekeyMap = new HashMap<String,String>();
	
	// TODO Need to implement correct storage and fetch for users and service keys.
	AuthenticationManager(){
		userMap.put("admin", "admin");
		servicekeyMap.put("admin", "ABC123");
	}
	
	
	public void getAuthentication(String username,String password, String servicekey){
		if(servicekey == null) {
			this.getAuthentication(username, password);
		} else {
			if(servicekey.equals(servicekeyMap.get(username))){
				if(username != null || username != "" && encryptDecryptService.decrypt(password).equals(userMap.get(username))){
					this.auth = true;
					
				}
				
			}
		}
	}
	
	private void getAuthentication(String username,String password){
		if(username != null || username != "" && password.equals(userMap.get(username))){
			
			LOGGER.info("Please use valid service key");
		}
	}
	
	public boolean isAuthenticated(){
		return auth;
	}

}
