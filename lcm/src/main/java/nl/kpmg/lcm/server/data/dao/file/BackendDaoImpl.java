package nl.kpmg.lcm.server.data.dao.file;

import java.util.logging.Logger;

import nl.kpmg.lcm.server.data.BackendModel;
import nl.kpmg.lcm.server.data.dao.BackendDao;
import nl.kpmg.lcm.server.data.dao.DaoException;

/**
 * Implementation for BackendDao
 * @author kos
 */
public class BackendDaoImpl extends AbstractGenericFileDaoImpl<BackendModel> implements BackendDao {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(BackendDaoImpl.class.getName());    

    /**
     * @param storagePath The path where the backend is stored
     * @throws DaoException when the storagePath doesn't exist
     */
    public BackendDaoImpl(final String storagePath) throws DaoException {
        super(storagePath, BackendModel.class);    	
    }    
	
	/**
	 * Update the original BackendModel with updated BackendModel
	 * @see nl.kpmg.lcm.server.data.dao.file.AbstractGenericFileDaoImpl#update(nl.kpmg.lcm.server.data.AbstractModel, nl.kpmg.lcm.server.data.AbstractModel)
	 */
	@Override
	protected void update(BackendModel original, BackendModel update) {
		original.setName(update.getName());
		original.setOptions(update.getOptions());	
	}

}
