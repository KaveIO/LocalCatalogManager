package nl.kpmg.lcm.server.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import nl.kpmg.lcm.server.rest.authentication.PasswordHash;

/**
 * User class to store User details.
 *
 * @author venkateswarlub
 *
 */
@JsonIgnoreProperties({"hashed"})
public class User extends AbstractModel {

    private String password;

    private String role;

    private boolean hashed = false;

    public void setPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.setPassword(password, false);
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

    public boolean passwordEquals(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (!hashed) {
           // fucked
        }
        if (this.password != null && password != null) {
            return PasswordHash.validatePassword(password, this.password);
        }
        return false;
    }

    public void hashPassword() throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (!hashed) {
            setPassword(password, true);
            hashed = true;
        } else {
            // fucked
        }
    }

    public boolean isHashed() {
        return hashed;
    }

    public void setHashed(boolean passwordIsHashed) {
        this.hashed = passwordIsHashed;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
