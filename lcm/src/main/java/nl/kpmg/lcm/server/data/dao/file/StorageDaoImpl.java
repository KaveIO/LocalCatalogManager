package nl.kpmg.lcm.server.data.dao.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.dao.StorageDao;
import nl.kpmg.lcm.server.data.dao.DaoException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author kos
 */
public class StorageDaoImpl implements StorageDao {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(StorageDaoImpl.class.getName());

    /**
     * Path where the task is stored.
     */
    private final File storage;

    /**
     * Object mapper used to serialize and de-serialize the task.
     */
    private final ObjectMapper mapper;

    /**
     * @param storagePath The path where the backend is stored
     * @throws DaoException when the storagePath doesn't exist
     */
    @Autowired
    public StorageDaoImpl(final String storagePath, final ObjectMapper mapper) throws DaoException {
        this.storage = new File(storagePath);
        this.mapper = mapper;

        if (!storage.isDirectory() || !this.storage.canWrite()) {
            throw new DaoException(String.format(
                    "The storage path %s is not a directory or not writable.", storage.getAbsolutePath()));
        }
    }

    /**
     * @param name The name of the backend
     * @return File with the given name
     */
    private File getBackendFile(String name) {
        return new File(String.format("%s/%s", storage, name));

    }


    @Override
    public List<Storage> getAll() {
        String[] allBackEndNames = storage.list();
        LinkedList<Storage> result = new LinkedList();

        for (String backEndName : allBackEndNames) {
            Storage backEnd = getByName(backEndName);
            if (backEnd != null) {
                result.add(backEnd);
            }
        }
        return result;
    }

    @Override
    public Storage getByName(String name) {
           try {
            Storage backEnd = mapper.readValue(getBackendFile(name), Storage.class);
            if (backEnd != null) {
                backEnd.setName(name);
            }
            return backEnd;
        } catch (IOException ex) {
            Logger.getLogger(StorageDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }


    @Override
    public void persist(Storage backEnd) {
        String name = backEnd.getName();

        if (name == null) {
            LOGGER.warning("Backend has no name identifying it.");

        }

        try {
            mapper.writeValue(getBackendFile(name), backEnd);
            backEnd.setName(name);
        }
        catch (IOException ex) {
            Logger.getLogger(TaskDescriptionDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void delete(Storage backend) {
        File backEndFile = getBackendFile(backend.getName());
        backEndFile.delete();
    }

}
