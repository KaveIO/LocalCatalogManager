package nl.kpmg.lcm.server.data;

import java.util.Map;

/**
 *
 * @author kos
 */
public class Storage extends AbstractModel {

    private Map options;

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
