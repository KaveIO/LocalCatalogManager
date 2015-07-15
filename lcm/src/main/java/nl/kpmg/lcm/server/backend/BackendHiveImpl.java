/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.kpmg.lcm.server.backend;

import java.io.InputStream;
import nl.kpmg.lcm.server.data.MetaData;

/**
 *
 * @author jpavel
 */
public class BackendHiveImpl extends AbstractBackend {

    /**
     * Address of the Hive server.
     *
     * @param storagePath is the server address.
     */
    private final String storagePath;
    
    /**
     * Default constructor.
     *
     * @param storagePath is the server address.
     */
    public BackendHiveImpl(final String storagePath) {
        this.storagePath = storagePath;
    }
    
    /**
     * Returns scheme supported by URI for this backend.
     *
     * @return "hive" string
     */
    @Override
    protected final String getSupportedUriSchema() {
        return "jdbc:hive";
    }
    

    @Override
    public DataSetInformation gatherDataSetInformation(MetaData metadata) throws BackendException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void store(MetaData metadata, InputStream content) throws BackendException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /**
     * Executes query specified in metadata, saves output locally and opens
     * @param metadata
     * @return
     * @throws BackendException 
     */
    @Override
    public InputStream read(MetaData metadata) throws BackendException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean delete(MetaData metadata) throws BackendException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
