package nl.kpmg.lcm.server.data;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author kos
 */


public class BackendModel {
    /**
     *
     */
    private Integer id;

    private String name;

    private Map options;


    /**
     *
     * @return id
     */
    public Integer getId() {
        return id;
    }
    /**
     *
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return name
     */
    public String getName() {
        return name;
    }
    /**
    *
    * @param name
    */
    public void setName(String name) {
        this.name = name;
    }
    /**
     *
     * @return options
     */
    public Map getOptions() {
        return options;
    }

    public void setOptions(Map options) {
        this.options = options;
    }

}
