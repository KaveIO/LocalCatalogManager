package nl.kpmg.lcm.server.data.dao.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.kpmg.lcm.server.JacksonJsonProvider;
import nl.kpmg.lcm.server.data.AbstractModel;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.GenericDao;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class  AbstractGenericFileDaoImpl<T extends AbstractModel> implements GenericDao<T>  {
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
		File objFile = new File(String.format("%s/%s", storage, id));							
		T obj = null;
		Class<T> obj1 = null;
		try {
			mapper = jacksonJsonProvider.getContext(AbstractModel.class);
			obj = (T) mapper.readValue(objFile, AbstractModel.class );
		} catch (JsonParseException e) {
			LOGGER.warning(e.getMessage());
		} catch (JsonMappingException e) {
			LOGGER.warning(e.getMessage());
		} catch (IOException e) {
			LOGGER.warning(e.getMessage());
		}
		return obj;
	}

	@Override
	public List<T> getAll() {
		File[] objFiles = storage.listFiles();
		List<T> objs = new ArrayList<T>();
		for (File objFile : objFiles) {
			T obj = null;			
			try {
				mapper = jacksonJsonProvider.getContext(AbstractModel.class);
				obj = (T) mapper.readValue(objFile, AbstractModel.class );
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
		File[] objFiles = storage.listFiles();
		List<T> objs = new ArrayList<T>();
		for (File objFile : objFiles) {
			T obj = null;			
			try {
				mapper = jacksonJsonProvider.getContext(AbstractModel.class);
				obj = (T) mapper.readValue(objFile, AbstractModel.class );
				if((obj.getName()).equals(name)){
					return obj;
				}
			} catch (JsonParseException e) {
				LOGGER.warning(e.getMessage());
			} catch (JsonMappingException e) {
				LOGGER.warning(e.getMessage());
			} catch (IOException e) {
				LOGGER.warning(e.getMessage());
			}
			
		}		
		return null;
	}

	@Override
	public void persist(T  obj) {
		File objFile = new File(String.format("%s/%s", storage, obj.getId()));					
				
		try {
			mapper = jacksonJsonProvider.getContext(AbstractModel.class);
			mapper.writeValue(objFile, obj.getClass());
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
		File[] objFiles = storage.listFiles();
		List<T> objs = new ArrayList<T>();
		for (File objFile : objFiles) {
			T obj1 = null;
			
			try {
				mapper = jacksonJsonProvider.getContext(AbstractModel.class);
				obj1 = (T) mapper.readValue(objFile,AbstractModel.class);
				if(obj1.equals(obj)){					
					update(obj1, obj);
					
					mapper.writeValue(objFile, obj1);
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

	protected abstract void update (T original, T update); 
	
	@Override
	public void delete(T obj) {
		File[] objFiles = storage.listFiles();
		List<T> objs = new ArrayList<T>();
		for (File objFile : objFiles) {
			T obj1 = null;			
			try {
				mapper = jacksonJsonProvider.getContext(AbstractModel.class);
				obj1 = (T) mapper.readValue(objFile, AbstractModel.class );
				if(obj1.equals(obj)){
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
		
}
