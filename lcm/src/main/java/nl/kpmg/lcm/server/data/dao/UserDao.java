package nl.kpmg.lcm.server.data.dao;

import java.util.List;

import nl.kpmg.lcm.server.data.User;

/**
 * User DAO Interface
 * @author venkateswarlub
 *
 */
public interface UserDao {				
	public List<User> getUsers();		
	public User getUser(String username);
	public void modifyUser(User user);
	public void deleteUser(String username);
	public void saveUser(User user);
}