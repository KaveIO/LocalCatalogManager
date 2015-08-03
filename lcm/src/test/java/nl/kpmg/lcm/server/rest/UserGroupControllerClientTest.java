package nl.kpmg.lcm.server.rest;

import nl.kpmg.lcm.server.AuthenticationManager;
import nl.kpmg.lcm.server.ServerException;
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
    public void testSaveUserGroup() throws ServerException{
    	UserGroup userGroup = new UserGroup();
    	userGroup.setUserGroup("testUserGroup1");    	
    	UserGroupController uc = new UserGroupController();
    	uc.setUserGroupService(userGroupService);
    	uc.setAuthenticationManager(am);
    	uc.saveUserGroup(userGroup,"AUTH_TOKEN","ABC123");            	
    }
    
    @Test
    public void testModifyUserGroup() throws ServerException{
    	UserGroup userGroup = new UserGroup();
    	userGroup.setUserGroup("testUserGroup1");
    	
    	UserGroupController uc = new UserGroupController();
    	uc.setUserGroupService(userGroupService);
    	uc.setAuthenticationManager(am);
    	uc.modifyUserGroup(userGroup,"AUTH_TOKEN","ABC123");            
    }
   
    @Test
    public void testDeleteUserGroup() throws ServerException{
    	UserGroupController uc = new UserGroupController();
    	uc.setUserGroupService(userGroupService);
    	uc.setAuthenticationManager(am);
    	uc.deleteUserGroup("testUserGroup1","AUTH_TOKEN","ABC123");
    }        
}