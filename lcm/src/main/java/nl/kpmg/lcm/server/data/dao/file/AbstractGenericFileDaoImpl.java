package nl.kpmg.lcm.server.data.dao.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import nl.kpmg.lcm.server.JacksonJsonProvider;
import nl.kpmg.lcm.server.data.AbstractModel;
import nl.kpmg.lcm.server.data.dao.DaoException;
import nl.kpmg.lcm.server.data.dao.GenericDao;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation for GenericDao
 * @author venkat
 *
 * @param <T> Generic Model class
 */
public abstract class  AbstractGenericFileDaoImpl<T extends AbstractModel> implements GenericDao<T>  {
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
	private ObjectMapper mapper;
	private JacksonJsonProvider jacksonJsonProvider;		
	private Class<T> clazz;
	
	/**
	 * Construction to take storagePath and Model Class Type from Sub Class
	 * @param storagePath storage location for model classes
	 * @param clazz Model class
	 * @throws DaoException
	 */
	protected AbstractGenericFileDaoImpl(String storagePath, Class<T> clazz) throws DaoException{		
		storage = new File(storagePath);
		this.clazz  = clazz;
		jacksonJsonProvider = new JacksonJsonProvider();		

		if (!storage.isDirectory() || !this.storage.canWrite()) {
			throw new DaoException(String.format(
					"The storage path %s is not a directory or not writable.",
					storage.getAbsolutePath()));
		}
	}
	/**
	 * Get the object by ID of the object
	 * @param id id field of the object 
	 * @see nl.kpmg.lcm.server.data.dao.GenericDao#getById(java.lang.Integer)
	 */
	@Override	
	public T getById(Integer id) {
		File objFile = new File(String.format("%s/%s", storage, id));							
		T obj = null;		
		try {
			mapper = jacksonJsonProvider.getContext(clazz);
			obj =  mapper.readValue(objFile, clazz);
		} catch (JsonParseException e) {
			LOGGER.warning(e.getMessage());
		} catch (JsonMappingException e) {
			LOGGER.warning(e.getMessage());
		} catch (IOException e) {
			LOGGER.warning(e.getMessage());
		}
		return obj;
	}

	/**
	 * Get all the objects from storage 
	 * @see nl.kpmg.lcm.server.data.dao.GenericDao#getAll()
	 */
	@Override	
	public List<T> getAll() {
		File[] objFiles = storage.listFiles();
		List<T> objs = new ArrayList<T>();
		for (File objFile : objFiles) {			
			T obj = null;			
			try {
				mapper = jacksonJsonProvider.getContext(clazz);
				obj = mapper.readValue(objFile, clazz );
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
	 * Get the object by using object name
	 * @param name Name field in the object
	 * @see nl.kpmg.lcm.server.data.dao.GenericDao#getByName(java.lang.String)
	 */
	@Override    
	public T getByName(String name) {
		File[] objFiles = storage.listFiles();
		
		for (File objFile : objFiles) {
			T obj = null;			
			try {
				mapper = jacksonJsonProvider.getContext(clazz);
				obj = mapper.readValue(objFile, clazz );
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

	/**
	 * Persist the object to storage location
	 * @see nl.kpmg.lcm.server.data.dao.GenericDao#persist(nl.kpmg.lcm.server.data.AbstractModel)
	 */
	@Override	
	public void persist(T  obj) {
		Integer newId = 0;
		if(obj.getId() == null){
			String[] idStrs = storage.list();
			Integer[] ids = new Integer[idStrs.length];
			int i=0;
			for(String str : idStrs){
				ids[i++] = Integer.parseInt(str);
			}
			if(ids.length !=0){
				Arrays.sort(ids);			
				newId = ids[ids.length-1]+1;
			}
			obj.setId(newId);
		} else {
			newId = obj.getId();
		}
		
		File objFile = new File(String.format("%s/%s", storage, newId));					
				
		try {
			mapper = jacksonJsonProvider.getContext(clazz);
			mapper.writeValue(objFile, obj);
		} catch (JsonParseException e) {
			LOGGER.warning(e.getMessage());
		} catch (JsonMappingException e) {
			LOGGER.warning(e.getMessage());
		} catch (IOException e) {
			LOGGER.warning(e.getMessage());
		}
		
	}

	/**
	 * Update the object in storage location
	 * @see nl.kpmg.lcm.server.data.dao.GenericDao#update(nl.kpmg.lcm.server.data.AbstractModel)
	 */
	@Override	
	public void update(T obj) {
		File[] objFiles = storage.listFiles();
		
		for (File objFile : objFiles) {
			T obj1 = null;
			
			try {
				mapper = jacksonJsonProvider.getContext(clazz);
				obj1 = mapper.readValue(objFile,clazz);
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

	/**
	 * To be implemented by sub class
	 * @param original
	 * @param update
	 */
	protected abstract void update (T original, T update); 
	
	/**
	 * Delete the object from storage
	 * @see nl.kpmg.lcm.server.data.dao.GenericDao#delete(nl.kpmg.lcm.server.data.AbstractModel)
	 */
	@Override	
	public void delete(T obj) {
		File[] objFiles = storage.listFiles();
		
		for (File objFile : objFiles) {
			T obj1 = null;			
			try {
				mapper = jacksonJsonProvider.getContext(clazz);
				obj1 = mapper.readValue(objFile, clazz );
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
