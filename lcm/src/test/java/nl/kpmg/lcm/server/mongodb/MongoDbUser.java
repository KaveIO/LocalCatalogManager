package nl.kpmg.lcm.server.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="users")
public class MongoDbUser {
	
	@Id
	private String id;
	
	String user;
	
	String password;
	
	public MongoDbUser(){
		
	}
	
	public MongoDbUser(String user, String password){
		this.user = user;
		this.password = password;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "MongoDbUser [id=" + id + ", user=" + user + ", password="
				+ password + "]";
	}
	

}
