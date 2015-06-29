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
package nl.kpmg.lcm.server.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import nl.kpmg.lcm.server.metadata.MetaData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

/**
 *
 * @author jpavel
 */
public class BackendHDFSImpl extends AbstractBackend {
    /**
     * Address of the HDFS server.
     * @param storagePath is the server address.
     */
    private final String storagePath;

    /**
     * Default constructor.
     * @param storagePath is the server address.
     */
    public BackendHDFSImpl(final String storagePath) {
        this.storagePath = storagePath;
    }

    /** Returns scheme supported by URI for this backend.
     *
     * @return "hdfs" string
     */
    @Override
    protected final String getSupportedUriSchema() {
       return "hdfs";
    }
    
    private FileSystem getPathFromUri(final String uri) throws BackendException, IOException {
        URI dataUri;
        
        if (uri != null) {
            dataUri = parseUri(uri);
            Configuration conf = new Configuration();
            conf.set("fs.default.name", storagePath);
            FileSystem file = FileSystem.get(dataUri, conf);
            /**
             * @TODO This is super scary. we should check if the resulting path
             * is still within storagePath
             */
            return file;
        } else {
            throw new BackendException("No URI specified.");
        }
    }

    @Override
    public DataSetInformation gatherDataSetInformation(MetaData metadata) throws BackendException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void store(MetaData metadata, InputStream content) throws BackendException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public InputStream read(MetaData metadata) throws BackendException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean delete(MetaData metadata) throws BackendException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
