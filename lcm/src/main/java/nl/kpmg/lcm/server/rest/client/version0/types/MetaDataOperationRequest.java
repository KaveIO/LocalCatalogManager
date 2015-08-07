package nl.kpmg.lcm.server.rest.client.version0.types;

import java.util.Map;

/**
 *
 * @author kos
 */
public class MetaDataOperationRequest {

    private String operation;

    private String type;

    private Map parameters;

    private String storagePath;

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map getParameters() {
        return parameters;
    }

    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }

    
}
