package nl.kpmg.lcm.server.rest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.kpmg.lcm.server.JacksonJsonProvider;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.service.UserService;
import nl.kpmg.lcm.server.rest.client.version0.UserController;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;


public class UserControllerClientTest extends LCMBaseTest {
   
	@Autowired
	private UserService userService;
	
    /**
     * Test to see that the client interface returns the interface versions.
     */
    @Test
    public void testGetUsers() {
    	UserController uc = new UserController();
    	uc.setUserService(userService);
    	uc.getUsers();
    	
        System.out.println(uc.getUsers());
    }
    
    //@Test
    public void testGetUser(){
    	UserController uc = new UserController();
    	uc.setUserService(userService);
    	uc.getUser("testUser1");
    	
        System.out.println(uc.getUser("testUser1"));
    }
    
    
    //@Test
    public void testSaveUser(){
    	User user = new User();
    	user.setUsername("testUser1");
    	user.setPassword("testPassword");
    	UserController uc = new UserController();
    	uc.setUserService(userService);
    	uc.saveUser(user);
        System.out.println(uc.getUser("testUser1"));    	
    }
    
    //@Test
    public void testModifyUser(){
    	User user = new User();
    	user.setUsername("testUser1");
    	user.setPassword("testPassword");
    	UserController uc = new UserController();
    	uc.setUserService(userService);
    	uc.saveUser(user);
        System.out.println(uc.getUser("testUser1"));
    	
    }
   
    //@Test
    public void testDeleteUser(){
    	
    }
    
    //@Test
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
}