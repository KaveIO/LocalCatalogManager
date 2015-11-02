package nl.kpmg.lcm.server.data.dao.file;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.UserDao;

/**
 * User DAO Implementation.
 *
 * @author venkateswarlub
 *
 */
public class UserDaoImpl extends AbstractGenericFileDaoImpl<User> implements UserDao {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(UserDaoImpl.class.getName());

    /**
     * @param storagePath The path where the user is stored
     * @throws DaoException when the storagePath doesn't exist
     */
    public UserDaoImpl(final String storagePath) throws DaoException {
        super(storagePath, User.class);
    }

    @Override
    public void persist(User object) {
        try {
            object.hashPassword();
            super.persist(object);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(UserDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Update the original User with updated User
     *
     * @param original User
     * @param update User
     * @see
     * nl.kpmg.lcm.server.data.dao.file.AbstractGenericFileDaoImpl#update(nl.kpmg.lcm.server.data.AbstractModel,
     * nl.kpmg.lcm.server.data.AbstractModel)
     */
    @Override
    protected void update(User original, User update) {
        try {
            original.setPassword(update.getPassword(), true);
            original.setHashed(true);
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.warning("Error while hashing user password.");
        } catch (InvalidKeySpecException ex) {
            LOGGER.warning("Error while hashing user password.");
        }
    }

    @Override
    public boolean isValid(User object) {
        if (object.getId() != null) {
            return true;
        }
        return false;
    }
}
