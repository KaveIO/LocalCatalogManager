package nl.kpmg.lcm.server.rest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import nl.kpmg.lcm.server.AuthenticationManager;
import nl.kpmg.lcm.server.ServerException;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.UserGroup;
import nl.kpmg.lcm.server.data.service.UserGroupService;
import nl.kpmg.lcm.server.rest.client.version0.UserGroupController;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class UserGroupControllerClientTest extends LCMBaseTest {
   
	@Autowired
	private UserGroupService userGroupService;
	@Autowired
	private AuthenticationManager am;
	
    /**
     * Test to see that the client interface returns the interface versions.
     */
    @Test
    public void testGetUserGroups() {
    	UserGroupController uc = new UserGroupController();
    	uc.setUserGroupService(userGroupService);
    	uc.getUserGroups();    	       
    }
    
    @Test
    public void testGetUserGroup(){
    	UserGroupController uc = new UserGroupController();
    	uc.setUserGroupService(userGroupService);    	
    	uc.getUserGroup("testUserGroup1");    	        
    }
    
    @Test
    public void testGetUserGroupsWebTarget() throws ServerException{
    	List<User> users = new ArrayList<User>();
    	User user = new User();
    	user.setUsername("admin");
    	user.setPassword("admin");
    	users.add(user);
    	UserGroup userGroup = new UserGroup();
    	userGroup.setId(100);
    	userGroup.setUserGroup("administrator");
    	userGroup.setUsers(users);
        Entity<User> entity = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");
        am.getAuthentication(user.getUsername(), user.getPassword(), "ABC123");
        Response res = target
                .path("client/v0/users/login").queryParam("serviceKey", "ABC123")
                .request().post(entity);
        String authToken = res.readEntity(String.class);
        Response res1 = target
                .path("client/v0/userGroups").queryParam("serviceKey", "ABC123").queryParam("authorizationToken", authToken)
                .request().get();
        
        System.out.println("Response"+res.toString()+"*****"+" "+"************"+res1.toString());
        assertEquals(200, res.getStatus());
        assertEquals(200, res1.getStatus());            	
    }
    
    @Test
    public void testGetUserGroupWebTarget() throws ServerException{
    	List<User> users = new ArrayList<User>();
    	User user = new User();
    	user.setUsername("admin");
    	user.setPassword("admin");
    	users.add(user);
    	UserGroup userGroup = new UserGroup();
    	userGroup.setId(100);
    	userGroup.setUserGroup("administrator");
    	userGroup.setUsers(users);
        Entity<User> entity = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");
        am.getAuthentication(user.getUsername(), user.getPassword(), "ABC123");
        Response res = target
                .path("client/v0/users/login").queryParam("serviceKey", "ABC123")
                .request().post(entity);
        String authToken = res.readEntity(String.class);
        Response res1 = target
                .path("client/v0/userGroups/admin").queryParam("serviceKey", "ABC123").queryParam("authorizationToken", authToken)
                .request().get();
        
        System.out.println("Response"+res.toString()+"*****"+" "+"************"+res1.toString());
        assertEquals(200, res.getStatus());
        assertEquals(200, res1.getStatus());            	
    }
    
    @Test
    public void testSaveUserGroup() throws ServerException{
    	UserGroup userGroup = new UserGroup();
    	userGroup.setUserGroup("testUserGroup1");    	
    	UserGroupController uc = new UserGroupController();
    	uc.setUserGroupService(userGroupService);
    	//uc.setAuthenticationManager(am);
    	uc.saveUserGroup(userGroup,"AUTH_TOKEN","ABC123");            	
    }
    
    @Test
    public void testSaveUserGroupWebTarget() throws ServerException{
    	List<User> users = new ArrayList<User>();
    	User user = new User();
    	user.setUsername("admin");
    	user.setPassword("admin");
    	users.add(user);
    	UserGroup userGroup = new UserGroup();
    	userGroup.setId(100);
    	userGroup.setUserGroup("testUserGroup");
    	userGroup.setUsers(users);
        Entity<User> entity = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");
        am.getAuthentication(user.getUsername(), user.getPassword(), "ABC123");
        Response res = target
                .path("client/v0/users/login").queryParam("serviceKey", "ABC123")
                .request().post(entity);
        String authToken = res.readEntity(String.class);
        Entity<UserGroup> entity1 = Entity.entity(userGroup, "application/nl.kpmg.lcm.server.data.UserGroup+json");
        Response res1 = target
                .path("client/v0/userGroups/testUserGroup").queryParam("serviceKey", "ABC123").queryParam("authorizationToken", authToken)
                .request().put(entity1);
        
        System.out.println("Response"+res.toString()+"*****"+" "+"************"+res1.toString());
        assertEquals(200, res.getStatus());
        assertEquals(200, res1.getStatus());            	
    }
    
    @Test
    public void testModifyUserGroup() throws ServerException{
    	UserGroup userGroup = new UserGroup();
    	userGroup.setUserGroup("testUserGroup1");
    	
    	UserGroupController uc = new UserGroupController();
    	uc.setUserGroupService(userGroupService);
    	//uc.setAuthenticationManager(am);
    	uc.modifyUserGroup(userGroup,"AUTH_TOKEN","ABC123");            
    }
    
    @Test
    public void testModifyUserGroupWebTarget() throws ServerException{
    	List<User> users = new ArrayList<User>();
    	User user = new User();
    	user.setUsername("admin");
    	user.setPassword("admin");
    	users.add(user);
    	UserGroup userGroup = new UserGroup();
    	userGroup.setId(101);
    	userGroup.setUserGroup("testUserGroup");
    	userGroup.setUsers(users);
        Entity<User> entity = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");
        am.getAuthentication(user.getUsername(), user.getPassword(), "ABC123");
        Response res = target
                .path("client/v0/users/login").queryParam("serviceKey", "ABC123")
                .request().post(entity);
        
        String authToken = res.readEntity(String.class);
        userGroup.setId(105);
        Entity<UserGroup> entity1 = Entity.entity(userGroup, "application/nl.kpmg.lcm.server.data.UserGroup+json");
        Response res1 = target
                .path("client/v0/userGroups/testUserGroup").queryParam("serviceKey", "ABC123").queryParam("authorizationToken", authToken)
                .request().put(entity1);
        
        System.out.println("Response"+res.toString()+"*****"+" "+"************"+res1.toString());
        assertEquals(200, res.getStatus());
        assertEquals(200, res1.getStatus());            	
    }
   
    @Test
    public void testDeleteUserGroup() throws ServerException{
    	UserGroupController uc = new UserGroupController();
    	uc.setUserGroupService(userGroupService);
    	//uc.setAuthenticationManager(am);
    	UserGroup userGroup = new UserGroup();
    	userGroup.setId(101);
    	userGroup.setUserGroup("testUserGroup1");
    	uc.deleteUserGroup("testUserGroup1","AUTH_TOKEN","ABC123");
    } 
    
    @Test
    public void testDeleteUserGroupWebTarget() throws ServerException{
    	List<User> users = new ArrayList<User>();
    	User user = new User();
    	user.setId(1);
    	user.setUsername("admin");
    	user.setPassword("admin");
    	users.add(user);
    	UserGroup userGroup = new UserGroup();
    	userGroup.setId(100);
    	userGroup.setUserGroup("testUserGroup1");
    	userGroup.setUsers(users);
        Entity<User> entity = Entity.entity(user, "application/nl.kpmg.lcm.server.data.User+json");
        am.getAuthentication(user.getUsername(), user.getPassword(), "ABC123");
        Response res = target
                .path("client/v0/users/login").queryParam("serviceKey", "ABC123")
                .request().post(entity);
        String authToken = res.readEntity(String.class);
        Entity<UserGroup> entity1 = Entity.entity(userGroup, "application/nl.kpmg.lcm.server.data.UserGroup+json");
        Response res1 = target
                .path("client/v0/userGroups/testUserGroup1").queryParam("serviceKey", "ABC123").queryParam("authorizationToken", authToken)
                .request().put(entity1);
        Response res2 = target
                .path("client/v0/userGroups/testUserGroup1").queryParam("serviceKey", "ABC123").queryParam("authorizationToken", authToken)
                .request().delete();
        
        System.out.println("Response"+res.toString()+"*****"+" "+"************"+res1.toString()+"****"+res2.toString());
        assertEquals(200, res.getStatus());
        assertEquals(200, res1.getStatus());
        assertEquals(200, res2.getStatus());
    }
}