package nl.kpmg.lcm.server.data.dao.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.kpmg.lcm.server.JacksonJsonProvider;
import nl.kpmg.lcm.server.data.User;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.GenericDao;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class  AbstractGenericFileDaoImpl<T> implements GenericDao<T>  {
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(AbstractGenericFileDaoImpl.class
			.getName());

	/**
	 * Path where the user is stored.
	 */
	private File storage = null;

	/**
	 * Object mapper used to serialize and de-serialize the user.
	 */
	private ObjectMapper mapper;
	private JacksonJsonProvider jacksonJsonProvider;	
	
	AbstractGenericFileDaoImpl(String storagePath) throws DaoException{		
		storage = new File(storagePath);

		jacksonJsonProvider = new JacksonJsonProvider();		

		if (!storage.isDirectory() || !this.storage.canWrite()) {
			throw new DaoException(String.format(
					"The storage path %s is not a directory or not writable.",
					storage.getAbsolutePath()));
		}
	}
	@Override
	public T getById(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> getAll() {
		File[] objFiles = storage.listFiles();
		List<T> objs = new ArrayList<T>();
		for (File objFile : objFiles) {
			T obj = null;
			Class<T> obj1 = null;
			try {
				mapper = jacksonJsonProvider.getContext(obj1);
				obj = mapper.readValue(objFile, obj1 );
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

	@Override
	public T getByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void persist(T obj) {
		File objFile = new File(String.format("%s/%s", storage, obj.toString()));					
		Class<T> obj1 = (Class) obj;		
		try {
			mapper = jacksonJsonProvider.getContext(obj1);
			mapper.writeValue(objFile, obj1);
		} catch (JsonParseException e) {
			LOGGER.warning(e.getMessage());
		} catch (JsonMappingException e) {
			LOGGER.warning(e.getMessage());
		} catch (IOException e) {
			LOGGER.warning(e.getMessage());
		}
		
	}

	@Override
	public void update(T obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(T obj) {
		// TODO Auto-generated method stub
		
	}
		
}
