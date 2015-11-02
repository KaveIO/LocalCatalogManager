package nl.kpmg.lcm.server.data.dao.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.kpmg.lcm.server.data.AbstractModel;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.GenericDao;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation for GenericDao
 *
 * @author venkat
 *
 * @param <T> Generic Model class
 */
public abstract class AbstractGenericFileDaoImpl<T extends AbstractModel> implements GenericDao<T> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(AbstractGenericFileDaoImpl.class
            .getName());

    /**
     * Path where the user is stored.
     */
    protected File storage = null;

    /**
     * Object mapper used to serialize and de-serialize the user.
     */
    @Autowired
    private ObjectMapper mapper;

    private Class<T> clazz;

    /**
     * Construction to take storagePath and Model Class Type from Sub Class
     *
     * @param storagePath storage location for model classes
     * @param clazz Model class
     * @throws DaoException
     */
    protected AbstractGenericFileDaoImpl(String storagePath, Class<T> clazz) throws DaoException {
        storage = new File(storagePath);
        this.clazz = clazz;

        if (!storage.isDirectory() || !this.storage.canWrite()) {
            throw new DaoException(String.format(
                    "The storage path %s is not a directory or not writable.",
                    storage.getAbsolutePath()));
        }
    }

    /**
     * Get the object by ID of the object
     *
     * @param id id field of the object
     * @see nl.kpmg.lcm.server.data.dao.GenericDao#getById(java.lang.Integer)
     */
    @Override
    public T getById(String id) {
        T obj = null;
        try {
            obj = mapper.readValue(getObjectFile(id), clazz);
        } catch (JsonParseException | JsonMappingException e) {
            LOGGER.warning(e.getMessage());
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        return obj;
    }

    /**
     * Get all the objects from storage
     *
     * @see nl.kpmg.lcm.server.data.dao.GenericDao#getAll()
     */
    @Override
    public List<T> getAll() {
        File[] objFiles = storage.listFiles();
        List<T> objs = new ArrayList<T>();
        for (File objFile : objFiles) {
            T obj = null;
            try {
                obj = mapper.readValue(objFile, clazz);
            } catch (JsonParseException e) {
                LOGGER.warning(e.getMessage());
            } catch (JsonMappingException e) {
                LOGGER.warning(e.getMessage());
            } catch (IOException e) {
                LOGGER.warning(e.getMessage());
            }
            if (obj != null) {
                objs.add(obj);
            }
        }
        return objs;
    }

    /**
     * Persist the object to storage location
     *
     * @see
     * nl.kpmg.lcm.server.data.dao.GenericDao#persist(nl.kpmg.lcm.server.data.AbstractModel)
     */
    @Override
    public void persist(T object) {
        try {
            if (isValid(object)) {
                if (object.getId() == null) {
                    autoGenerateId(object);
                }
                File objFile = new File(String.format("%s/%s", storage, object.getId()));
                mapper.writeValue(objFile, object);
            } else {
                LOGGER.warning("fail, throw me...");
            }
        } catch (JsonParseException | JsonMappingException e) {
            LOGGER.warning(e.getMessage());
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * Update the object in storage location
     *
     * @see
     * nl.kpmg.lcm.server.data.dao.GenericDao#update(nl.kpmg.lcm.server.data.AbstractModel)
     */
    @Override
    public void update(T update) {
        try {
            T original = getById(update.getId());
            if (original != null) {
                update(original, update);
                if (isValid(original)) {
                    mapper.writeValue(getObjectFile(original), original);
                }
            }
        } catch (JsonParseException | JsonMappingException e) {
            LOGGER.warning(e.getMessage());
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * To be implemented by sub class
     *
     * @param original
     * @param update
     */
    protected abstract void update(T original, T update);

    /**
     * Delete the object from storage
     *
     * @see
     * nl.kpmg.lcm.server.data.dao.GenericDao#delete(nl.kpmg.lcm.server.data.AbstractModel)
     */
    @Override
    public void delete(T obj) {
        File[] objFiles = storage.listFiles();

        for (File objFile : objFiles) {
            T obj1 = null;
            try {
                obj1 = mapper.readValue(objFile, clazz);
                if (obj1.equals(obj)) {
                    objFile.delete();
                }
            } catch (JsonParseException e) {
                LOGGER.warning(e.getMessage());
            } catch (JsonMappingException e) {
                LOGGER.warning(e.getMessage());
            } catch (IOException e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    public abstract boolean isValid(T object);

    private void autoGenerateId(T object) {
        if (object.getId() == null) {
            String randomAlphanumeric = RandomStringUtils.randomAlphanumeric(8);
            object.setId(String.format("%s-%s", new Date().getTime(), randomAlphanumeric));
        }
    }

    private File getObjectFile(T object) {
        return getObjectFile(object.getId());
    }

    private File getObjectFile(String id) {
        return new File(String.format("%s/%s", storage, id));
    }
}
