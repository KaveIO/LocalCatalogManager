package nl.kpmg.lcm.server.rest.client.types;

/**
 *
 * @author mhoekstra
 */
public class LoginRequest {

    private String username;

    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}