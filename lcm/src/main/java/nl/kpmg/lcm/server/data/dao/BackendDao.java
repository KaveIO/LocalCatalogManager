package nl.kpmg.lcm.server.data.dao;

import java.util.List;
import nl.kpmg.lcm.server.data.BackendModel;

/**
 *
 * @author kos
 */


public interface BackendDao {

    /**
     * @param host
     * @return BackendModel
     */
    public BackendModel getByName(String host);

    /**
     * @return list of all BackendModels
     */
    public List<BackendModel> getAll();

    /**
     *
     * @param backend
     */
    public void persist(BackendModel backend);

    /**
     *
     * @param backend
     */
    public void delete(BackendModel backend);
}
