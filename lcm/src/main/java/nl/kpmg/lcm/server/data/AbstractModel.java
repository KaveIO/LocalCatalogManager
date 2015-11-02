package nl.kpmg.lcm.server.data;

import java.util.Objects;

/**
 * AbsractModel class for generic model parameters
 *
 * @author venkateswarlub
 *
 */
public class AbstractModel {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractModel other = (AbstractModel) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}
