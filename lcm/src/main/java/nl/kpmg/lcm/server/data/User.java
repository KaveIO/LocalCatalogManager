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
@Document(collection = "users")
public class User {

    private String username;

    private String password;

    private String role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.setPassword(password, true);
    }

    public void setPassword(String password, boolean hashPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (hashPassword) {
            this.password = PasswordHash.createHash(password);
        } else {
            this.password = password;
        }
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean passwordEquals(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (this.password != null && password != null) {
            return PasswordHash.validatePassword(password, this.password);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((password == null) ? 0 : password.hashCode());
        result = prime * result
                + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        User other = (User) obj;
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (username == null) {
            if (other.username != null) {
                return false;
            }
        } else if (!username.equals(other.username)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "User [username=" + username + ", password=" + password + "]";
    }
}
