package nl.kpmg.lcm.server.data.dao.file;

import java.util.logging.Logger;

import nl.kpmg.lcm.server.data.UserGroup;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.UserGroupDao;

/**
 * UserGroup DAO Implementation.
 *
 * @author venkateswarlub
 *
 */
public class UserGroupDaoImpl extends AbstractGenericFileDaoImpl<UserGroup> implements UserGroupDao {

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(UserGroupDaoImpl.class
			.getName());
	
	/**
	 * @param storagePath
	 *            The path where the user is stored
	 * @throws DaoException
	 *             when the storagePath doesn't exist
	 */
	public UserGroupDaoImpl(final String storagePath) throws DaoException {
		super(storagePath,UserGroup.class);		
	}



	/**
	 * Update the original UserGroup with updated UserGroup 
	 * @param original UserGroup
	 * @param update UserGroup 
	 * @see nl.kpmg.lcm.server.data.dao.file.AbstractGenericFileDaoImpl#update(nl.kpmg.lcm.server.data.AbstractModel, nl.kpmg.lcm.server.data.AbstractModel)
	 */
	@Override
	protected void update(UserGroup original, UserGroup update) {		
		original.setName(update.getName());
		original.setUsers(update.getUsers());
	}
	
}
