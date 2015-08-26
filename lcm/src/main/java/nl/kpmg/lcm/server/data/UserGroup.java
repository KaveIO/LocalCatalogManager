package nl.kpmg.lcm.server.data;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * User Group class for storing User Group details.
 *
 * @author venkateswarlub
 *
 */
@Document(collection = "userGroups")
public class UserGroup {

    private int id;

    private String userGroup;

    private List<String> users;

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result
                + ((userGroup == null) ? 0 : userGroup.hashCode());
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
        UserGroup other = (UserGroup) obj;
        if (id != other.id) {
            return false;
        }
        if (userGroup == null) {
            if (other.userGroup != null) {
                return false;
            }
        } else if (!userGroup.equals(other.userGroup)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UserGroup [id=" + id + ", userGroup=" + userGroup + "]";
    }

}
