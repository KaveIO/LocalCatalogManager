package nl.kpmg.lcm.server.data.dao;

import java.util.List;

import nl.kpmg.lcm.server.data.UserGroup;

/**
 * User Group DAO interface
 * @author venkateswarlub
 *
 */
public interface UserGroupDao {
	public List<UserGroup> getUserGroups();
	public UserGroup getUserGroup(String userGroupname);
	public void saveUserGroup(UserGroup userGroup);
	public void modifyUserGroup(UserGroup userGroup);
	public void deleteUserGroup(String userGroupname);
}