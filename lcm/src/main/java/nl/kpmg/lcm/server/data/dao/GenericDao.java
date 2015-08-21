package nl.kpmg.lcm.server.data.dao;

import java.util.List;

/**
 * Generic Dao class
 * 
 * @author venkateswarlub
 *
 * @param <T> Specific Dao class
 */
public interface GenericDao<T> {
	 	
		public T getById(Integer id);
	    
	    public List<T> getAll();

	    public T getByName(String name);

	    public void persist(T obj);
	    
	    public void update(T obj);

	    public void delete(T obj);
	
}
