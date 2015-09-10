package nl.kpmg.lcm.server.data.dao;

import java.util.List;
import nl.kpmg.lcm.server.data.Storage;

/**
 * @author kos
 */
public interface StorageDao {

    /**
     * @param host
     * @return BackendModel
     */
    public Storage getByName(String host);

    /**
     * @return list of all BackendModels
     */
    public List<Storage> getAll();

    /**
     *
     * @param backend
     */
    public void persist(Storage backend);

    /**
     *
     * @param backend
     */
    public void delete(Storage backend);
}
