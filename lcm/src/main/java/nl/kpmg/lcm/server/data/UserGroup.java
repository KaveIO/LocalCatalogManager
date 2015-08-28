package nl.kpmg.lcm.server.data;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * User Group class for storing User Group details.
 * @author venkateswarlub
 *
 */
@Document(collection="userGroups")
public class UserGroup extends AbstractModel {		
	
	private List<User> users;	
	
	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}		
	
}
