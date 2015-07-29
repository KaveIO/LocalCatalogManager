package nl.kpmg.lcm.server.rest;

import nl.kpmg.lcm.server.data.UserGroup;
import nl.kpmg.lcm.server.data.service.UserGroupService;
import nl.kpmg.lcm.server.rest.client.version0.UserGroupController;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class UserGroupControllerClientTest extends LCMBaseTest {
   
	@Autowired
	private UserGroupService userGroupService;	
	
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
    public void testSaveUserGroup(){
    	UserGroup userGroup = new UserGroup();
    	userGroup.setUserGroup("testUserGroup1");    	
    	UserGroupController uc = new UserGroupController();
    	uc.setUserGroupService(userGroupService);
    	uc.saveUserGroup(userGroup);            	
    }
    
    @Test
    public void testModifyUserGroup(){
    	UserGroup userGroup = new UserGroup();
    	userGroup.setUserGroup("testUserGroup1");
    	
    	UserGroupController uc = new UserGroupController();
    	uc.setUserGroupService(userGroupService);
    	uc.modifyUserGroup(userGroup);            
    }
   
    @Test
    public void testDeleteUserGroup(){
    	UserGroupController uc = new UserGroupController();
    	uc.setUserGroupService(userGroupService);
    	uc.deleteUserGroup("testUserGroup1");
    }        
}