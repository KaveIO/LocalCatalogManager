package nl.kpmg.lcm.server.data.dao;

import java.util.List;

import nl.kpmg.lcm.server.data.AbstractModel;

/**
 * Generic Dao class
 *
 * @author venkateswarlub
 *
 * @param <T> Specific Dao class
 */
public interface GenericDao<T extends AbstractModel> {

    public T getById(String id);

    public List<T> getAll();

    public void persist(T obj);

    public void update(T obj);

    public void delete(T obj);

}
