package nl.kpmg.lcm.server.data.dao.file;

import java.util.logging.Logger;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.dao.StorageDao;
import nl.kpmg.lcm.server.data.dao.DaoException;

public class StorageDaoImpl extends AbstractGenericFileDaoImpl<Storage> implements StorageDao {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(StorageDaoImpl.class.getName());

    /**
     * @param storagePath The path where the backend is stored
     * @throws DaoException when the storagePath doesn't exist
     */
    public StorageDaoImpl(final String storagePath) throws DaoException {
        super(storagePath, Storage.class);
    }

    /**
     * Update the original BackendModel with updated BackendModel.
     *
     * @see
     * nl.kpmg.lcm.server.data.dao.file.AbstractGenericFileDaoImpl#update(nl.kpmg.lcm.server.data.AbstractModel,
     * nl.kpmg.lcm.server.data.AbstractModel)
     */
    @Override
    protected void update(Storage original, Storage update) {
        original.setOptions(update.getOptions());
    }

    @Override
    public boolean isValid(Storage object) {
        if (object.getId() != null) {
            return true;
        }
        return false;
    }
}
