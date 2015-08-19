package nl.kpmg.lcm.server.data.dao.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.kpmg.lcm.server.JacksonJsonProvider;
import nl.kpmg.lcm.server.data.UserGroup;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.UserGroupDao;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * UserGroup DAO Implementation.
 * @author venkateswarlub
 *
 */
public class UserGroupDaoImpl implements UserGroupDao {

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(UserGroupDaoImpl.class
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
	 * @param storagePath
	 *            The path where the user is stored
	 * @throws DaoException
	 *             when the storagePath doesn't exist
	 */
	public UserGroupDaoImpl(final String storagePath) throws DaoException {
		storage = new File(storagePath);

		jacksonJsonProvider = new JacksonJsonProvider();
		// mapper = jacksonJsonProvider.getContext(User.class);

		if (!storage.isDirectory() || !this.storage.canWrite()) {
			throw new DaoException(String.format(
					"The storage path %s is not a directory or not writable.",
					storage.getAbsolutePath()));
		}
	}

	private File getUserGroupFile(String name) {
		return new File(String.format("%s/%s", storage, name));
	}
	
	private String getUserGroupFromPath(String fileName) {
		String[] path = fileName.split("/");
		
		return path[path.length - 1];
	}

	@Override
	public List<UserGroup> getUserGroups() {
		File[] userFiles = storage.listFiles();
		List<UserGroup> userGroups = new ArrayList<UserGroup>();
		for (File userFile : userFiles) {
			UserGroup userGroup = null;
			try {
				mapper = jacksonJsonProvider.getContext(UserGroup.class);
				userGroup = mapper.readValue(userFile, UserGroup.class);
			} catch (JsonParseException e) {
				LOGGER.warning(e.getMessage());
			} catch (JsonMappingException e) {
				LOGGER.warning(e.getMessage());
			} catch (IOException e) {
				LOGGER.warning(e.getMessage());
			}
			if (userGroup != null) {
				userGroups.add(userGroup);
			}
		}
		return userGroups;
	}

	@Override
	public UserGroup getUserGroup(String userGroupName) {
		File[] userFiles = storage.listFiles();
		UserGroup userGroup = null;
		for (File userFile : userFiles) {
			
			if (getUserGroupFromPath(userFile.getName()).equals(userGroupName)) {
				try {
					mapper = jacksonJsonProvider.getContext(UserGroup.class);
					userGroup = mapper.readValue(userFile, UserGroup.class);
				} catch (JsonParseException e) {
					System.out.println(e);
					LOGGER.warning(e.getMessage());
				} catch (JsonMappingException e) {
					System.out.println(e);
					LOGGER.warning(e.getMessage());
				} catch (IOException e) {
					System.out.println(e);
					LOGGER.warning(e.getMessage());
				}
			}

		}
		return userGroup;
	}

	@Override
	public void modifyUserGroup(UserGroup userGroup) {
		File[] userFiles = storage.listFiles();
		UserGroup userGroupFromFile = null;
		for (File userFile : userFiles) {

			if (getUserGroupFromPath(userFile.getName()).equals(userGroup.getUserGroup())) {
				try {
					mapper = jacksonJsonProvider.getContext(UserGroup.class);
					userGroupFromFile = mapper.readValue(userFile, UserGroup.class);					
					userGroupFromFile.setId(userGroup.getId());
					userGroupFromFile.setUserGroup(userGroup.getUserGroup());
					userGroupFromFile.setUsers(userGroup.getUsers());
					mapper.writeValue(userFile, userGroupFromFile);
				} catch (JsonParseException e) {
					LOGGER.warning(e.getMessage());
				} catch (JsonMappingException e) {
					LOGGER.warning(e.getMessage());
				} catch (IOException e) {
					LOGGER.warning(e.getMessage());
				}
			}
		}

	}

	@Override
	public void deleteUserGroup(String userGroupName) {
		File[] userFiles = storage.listFiles();		
		for (File userFile : userFiles) {

			if (getUserGroupFromPath(userFile.getName()).equals(userGroupName)) {
				try {
					userFile.delete();
				} catch (Exception e) {
					LOGGER.warning(e.getMessage());
				}
			}
		}
	}

	@Override
	public void saveUserGroup(UserGroup userGroup) {
		File userFile = getUserGroupFile(userGroup.getUserGroup());

		try {
			mapper = jacksonJsonProvider.getContext(UserGroup.class);
			mapper.writeValue(userFile, userGroup);
		} catch (JsonParseException e) {
			LOGGER.warning(e.getMessage());
		} catch (JsonMappingException e) {
			LOGGER.warning(e.getMessage());
		} catch (IOException e) {
			LOGGER.warning(e.getMessage());
		}

	}
	

}
