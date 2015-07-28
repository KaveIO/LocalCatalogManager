package nl.kpmg.lcm.server.data.service;

import org.springframework.beans.factory.annotation.Autowired;

import nl.kpmg.lcm.server.data.dao.UserGroupDao;

/**
 * User Authentication Service
 * @author venkateswarlub
 *
 */
public class UserGroupService { 
	
	@Autowired
	private UserGroupDao userGroupDao;
	
	public void setUserGroupDao(UserGroupDao userGroupDao){
		this.userGroupDao = userGroupDao;
	}
	
	public UserGroupDao getUserGroupDao() {
		
		return userGroupDao;
	}
	
	
}
