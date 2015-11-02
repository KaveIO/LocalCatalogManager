package nl.kpmg.lcm.server.data;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import nl.kpmg.lcm.server.rest.authentication.PasswordHash;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * User class to store User details.
 *
 * @author venkateswarlub
 *
 */
@Document(collection="users")
public class User extends AbstractModel{ 		
			
	private String password;
		
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}		
	
}
