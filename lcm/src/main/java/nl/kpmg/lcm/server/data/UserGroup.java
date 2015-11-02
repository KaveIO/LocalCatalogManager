package nl.kpmg.lcm.server.data;

import java.util.List;

/**
 * User Group class for storing User Group details.
 *
 * @author venkateswarlub
 *
 */
public class UserGroup extends AbstractModel {

    private List<String> users;

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
