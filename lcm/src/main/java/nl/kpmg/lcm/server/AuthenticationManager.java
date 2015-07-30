package nl.kpmg.lcm.server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
	Map<String,String> authorizationTokenMap = new HashMap<String,String>();
	
	// TODO Need to implement correct storage and fetch for users and service keys.
	AuthenticationManager(){
		userMap.put("admin", "admin");
		servicekeyMap.put("ABC123", "admin");
		authorizationTokenMap.put("AUTH_TOKEN", "admin");
	}
	
	
	public String getAuthentication(String username,String password, String servicekey) throws ServerException{		
		if(servicekey == null) {
			this.getAuthentication(username, password);
		} else {
			if(servicekeyMap.containsKey(servicekey)){
				String usernameMatch = servicekeyMap.get(servicekey);
				if(usernameMatch.equals(username)&& userMap.containsKey(username)){
					String passwordMatch = userMap.get(username);
					if(passwordMatch.equals(password)){
						this.auth = true;
						String authorizationToken = UUID.randomUUID().toString();
						authorizationTokenMap.put(authorizationToken, username);
						return authorizationToken;
					}
				}
				
			}
		}
		throw new ServerException("Authentication Failed");
	}
	
	public boolean isAuthorizationTokenValid(String serviceKey, String authourizationToken){
		if(isServiceKeyValid(serviceKey)){
			String usernameMatch1 = servicekeyMap.get(serviceKey);
			if(authorizationTokenMap.containsKey(authourizationToken)){
				String usernameMatch2 = authorizationTokenMap.get(authourizationToken);
				if(usernameMatch1.equals(usernameMatch2)){
					return true;
				}
			}
		}
		return false;
	}
	
	
	public boolean isServiceKeyValid(String serviceKey){		
		return servicekeyMap.containsKey(serviceKey);
	}
	
	private void getAuthentication(String username,String password){
		if(username != null || username != "" && password.equals(userMap.get(username))){
			
			LOGGER.info("Please use valid service key");
		}
	}
	
	public boolean isAuthenticated(){
		return auth;
	}


	public boolean logout(String serviceKey, String authourizationToken)
			throws ServerException {
		if(authorizationTokenMap.containsKey(authourizationToken)){
		authorizationTokenMap.remove(authourizationToken);
		return true;
		}
		throw new ServerException("logout Failed");
	}

}
