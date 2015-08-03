package nl.kpmg.lcm.server.rest;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.kpmg.lcm.server.AuthenticationManager;
import nl.kpmg.lcm.server.JacksonJsonProvider;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.service.EncryptDecryptService;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.client.version0.UserController;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;


public class UserControllerClientTest extends LCMBaseTest {
   
	@Autowired
	private UserService userService;
	@Autowired
	private AuthenticationManager am;
	@Autowired
	private EncryptDecryptService encdecService;
	
	
	//@Test
    public void testGetUserTarget() {
        String expected = "";
        String actual = target
                .path("client\\v0\\users\\admin")
                .request()
                .get(String.class);
        assertEquals(expected, actual);
    }
    /**
     * Test to see that the client interface returns the interface versions.
     */
    @Test
    public void testGetUsers() {
    	UserController uc = new UserController();
    	uc.setUserService(userService);
    	uc.getUsers();    	       
    }
    
    @Test
    public void testGetUser() throws ServerException{
    	UserController uc = new UserController();
    	uc.setUserService(userService);    	
    	uc.getUser("testUser1");    	        
    }
        
    @Test
    public void testSaveUser() throws ServerException{
    	User user = new User();
    	user.setUsername("testUser1");
    	user.setPassword("testPassword");
    	UserController uc = new UserController();
    	uc.setUserService(userService);
    	uc.setAuthenticationManager(am);
    	uc.saveUser(user,"AUTH_TOKEN","ABC123");            	
    }
    
    @Test
    public void testLoginUser() throws ServerException{
    	User user = new User();
    	user.setUsername("admin");
    	user.setPassword("admin");
    	UserController uc = new UserController();
    	uc.setUserService(userService);
    	uc.setAuthenticationManager(am);
    	uc.login(user, "ABC123");            	
    }
    
    @Test
    public void testLogoutUser() throws ServerException{
    	User user = new User();
    	user.setUsername("admin");
    	user.setPassword("admin");
    	UserController uc = new UserController();
    	uc.setUserService(userService);
    	uc.setAuthenticationManager(am);
    	uc.logout("AUTH_TOKEN","ABC123");            	
    }
    
    @Test
    public void testModifyUser() throws ServerException{
    	User user = new User();
    	user.setUsername("testUser1");
    	user.setPassword("testPassword");
    	UserController uc = new UserController();
    	uc.setUserService(userService);
    	uc.setAuthenticationManager(am);
    	uc.modifyUser(user,"AUTH_TOKEN","ABC123");            
    }
   
    @Test
    public void testDeleteUser() throws ServerException{
    	UserController uc = new UserController();
    	uc.setUserService(userService);
    	uc.setAuthenticationManager(am);
    	uc.deleteUser("testUser1","AUTH_TOKEN","ABC123");
    }
    
    @Test
    public void testJSONConversion(){
    	User user = new User();
    	user.setUsername("testUser1");
    	user.setPassword("testPassword1");
    	User user1 = new User();
    	user1.setUsername("testUser2");
    	user1.setPassword("testPassword2");
    	List<User> users = new ArrayList<User>();
    	users.add(user);
    	users.add(user1);
    	
    	JacksonJsonProvider jsonp = new JacksonJsonProvider();
    	ObjectMapper mapper = jsonp.getContext(List.class);
    	try {
			mapper.writeValue(new File("usersList"), users);
		} catch (IOException e) {			
			e.printStackTrace();
		}
    }    
}