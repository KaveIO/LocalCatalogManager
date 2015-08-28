package nl.kpmg.lcm.server.data.dao.file;

import java.util.logging.Logger;

import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.UserDao;

/**
 * User DAO Implementation.
 * @author venkateswarlub
 *
 */
public class UserDaoImpl extends AbstractGenericFileDaoImpl<User>  implements UserDao {

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(UserDaoImpl.class
			.getName());

	
	/**
	 * @param storagePath
	 *            The path where the user is stored
	 * @throws DaoException
	 *             when the storagePath doesn't exist
	 */
	public UserDaoImpl(final String storagePath) throws DaoException {
		super(storagePath);		
	}
	
	@Override
	protected void update(User original, User update) {
		original.setName(update.getName());
		original.setPassword(update.getPassword());
	}

}
