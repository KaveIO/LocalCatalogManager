package nl.kpmg.lcm.server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Authentication Manager
 * @author venkateswarlub
 *
 */
public class AuthenticationManager {
		
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationManager.class);
	private boolean auth = false;	
	
	//@Autowired
	//private EncryptDecryptService encryptDecryptService;	
	
	private UserService userService;

	private String serviceKeyKey;
	
	private String serviceKeyValue;
	
	private String authorizationTokenKey;
	
	private String authorizationTokenValue;
	
	private String adminUser;
	
	private String adminPassword;
	
	
	Map<String,String> userMap = new HashMap<String,String>();
	Map<String,String> servicekeyMap = new HashMap<String,String>();
	Map<String,String> authorizationTokenMap = new HashMap<String,String>();
			
	AuthenticationManager() {
				
	}
	private void init() {
		servicekeyMap.put(serviceKeyKey, serviceKeyValue);		
		authorizationTokenMap.put(authorizationTokenKey, authorizationTokenValue);
		userMap.put(adminUser, adminPassword);		
		for (User user : userService.getUserDao().getUsers()) {						
			userMap.put(user.getUsername(), user.getPassword());
		}	
	}

	@Autowired
	public void setUserService(UserService userService){
		this.userService = userService;
	}
	
	

	public String getServiceKeyKey() {
		return serviceKeyKey;
	}

	public void setServiceKeyKey(String serviceKeyKey) {
		this.serviceKeyKey = serviceKeyKey;
	}

	public String getServiceKeyValue() {
		return serviceKeyValue;
	}

	public void setServiceKeyValue(String serviceKeyValue) {
		this.serviceKeyValue = serviceKeyValue;
	}	

	public String getAuthorizationTokenKey() {
		return authorizationTokenKey;
	}

	public void setAuthorizationTokenKey(String authorizationTokenKey) {
		this.authorizationTokenKey = authorizationTokenKey;
	}

	public String getAuthorizationTokenValue() {
		return authorizationTokenValue;
	}

	public void setAuthorizationTokenValue(String authorizationTokenValue) {
		this.authorizationTokenValue = authorizationTokenValue;
	}		

	
	public void setAdminUser(String adminUser) {
		this.adminUser = adminUser;
	}
	
	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}
	public String getAuthentication(String username,String password, String servicekey) throws ServerException{				
		init();
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
		if(servicekeyMap.isEmpty()){
			init();
		}
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
		if(servicekeyMap.isEmpty()){
			init();
		}
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
