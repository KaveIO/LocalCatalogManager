package nl.kpmg.lcm.server.data.dao.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.kpmg.lcm.server.JacksonJsonProvider;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.UserDao;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;

/**
 * User DAO Implementation.
 *
 * @author venkateswarlub
 *
 */
public class UserDaoImpl implements UserDao {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(UserDaoImpl.class
            .getName());

    /**
     * Path where the user is stored.
     */
    private final File storage;

    /**
     * Object mapper used to serialize and de-serialize the user.
     */
    private ObjectMapper mapper;
    private JacksonJsonProvider jacksonJsonProvider;

    /**
     * @param storagePath The path where the user is stored
     * @throws DaoException when the storagePath doesn't exist
     */
    public UserDaoImpl(final String storagePath) throws DaoException {
        storage = new File(storagePath);

        jacksonJsonProvider = new JacksonJsonProvider();

        if (!storage.isDirectory() || !this.storage.canWrite()) {
            throw new DaoException(String.format(
                    "The storage path %s is not a directory or not writable.",
                    storage.getAbsolutePath()));
        }
    }

    private File getUserFile(String name) {
        return new File(String.format("%s/%s", storage, name));
    }

    private String getUsernameFromPath(String fileName) {
        String[] path = fileName.split("/");

        return path[path.length - 1];
    }

    @Override
    public List<User> getUsers() {
        File[] userFiles = storage.listFiles();
        List<User> users = new ArrayList<User>();
        for (File userFile : userFiles) {
            User user = null;
            try {
                mapper = jacksonJsonProvider.getContext(User.class);
                user = mapper.readValue(userFile, User.class);
            }
            catch (JsonParseException e) {
                LOGGER.warning(e.getMessage());
            }
            catch (JsonMappingException e) {
                LOGGER.warning(e.getMessage());
            }
            catch (IOException e) {
                LOGGER.warning(e.getMessage());
            }
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public User getUser(String username) {
        File[] userFiles = storage.listFiles();
        User user = null;
        for (File userFile : userFiles) {

            if (getUsernameFromPath(userFile.getName()).equals(username)) {
                try {
                    mapper = jacksonJsonProvider.getContext(User.class);
                    user = mapper.readValue(userFile, User.class);
                }
                catch (JsonParseException e) {
                    System.out.println(e);
                    LOGGER.warning(e.getMessage());
                }
                catch (JsonMappingException e) {
                    System.out.println(e);
                    LOGGER.warning(e.getMessage());
                }
                catch (IOException e) {
                    System.out.println(e);
                    LOGGER.warning(e.getMessage());
                }
            }

        }
        return user;
    }

    @Override
    public void modifyUser(User user) {
        File[] userFiles = storage.listFiles();
        User userFromFile = null;
        for (File userFile : userFiles) {

            if (getUsernameFromPath(userFile.getName()).equals(user.getUsername())) {
                try {
                    mapper = jacksonJsonProvider.getContext(User.class);
                    userFromFile = mapper.readValue(userFile, User.class);
                    userFromFile.setUsername(user.getUsername());

                    userFromFile.setPassword(user.getPassword(), false);

                    mapper.writeValue(userFile, userFromFile);
                } catch (JsonParseException | JsonMappingException e) {
                    LOGGER.warning(e.getMessage());
                } catch (IOException e) {
                    LOGGER.warning(e.getMessage());
                } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                    LOGGER.log(Level.SEVERE, "Something went wrong with the password hashing algorithm", ex);
                }
            }
        }

    }

    @Override
    public void deleteUser(String username) {
        File[] userFiles = storage.listFiles();
        for (File userFile : userFiles) {

            if (getUsernameFromPath(userFile.getName()).equals(username)) {
                try {
                    userFile.delete();
                }
                catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }
            }
        }
    }

    @Override
    public void saveUser(User user) {
        File userFile = getUserFile(user.getUsername());

        try {
            mapper = jacksonJsonProvider.getContext(User.class);
            mapper.writeValue(userFile, user);
        }
        catch (JsonParseException e) {
            LOGGER.warning(e.getMessage());
        }
        catch (JsonMappingException e) {
            LOGGER.warning(e.getMessage());
        }
        catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }

    }

}
