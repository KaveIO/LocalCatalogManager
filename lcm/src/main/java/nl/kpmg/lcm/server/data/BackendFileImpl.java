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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.metadata.MetaData;

/**
 *
 * @author mhoekstra
 */
public class BackendFileImpl extends AbstractBackend {

    private final File storagePath;

    public BackendFileImpl(File storagePath) {
        this.storagePath = storagePath;
    }

    private File getPathFromUri(String uri) throws BackendException {
        URI dataUri;
        /** @TODO Should we issue a warning via logger or exception in this case?  */ 
        if(uri!=null){
           dataUri = parseUri(uri);

        String filePath = dataUri.getPath();
        /** @TODO This is super scary. we should check if the resulting path is still within storagePath*/
        return new File(String.format("%s", filePath));
        } else return null;
    }

    @Override
    protected String getSupportedUriSchema() {
        return "file";
    }

    @Override
    public DataSetInformation gatherDataSetInformation(MetaData metadata) throws BackendException {
        File file = getPathFromUri(metadata.getDataUri());

        DataSetInformation dataSetInformation = new DataSetInformation();
        
        dataSetInformation.setUri(metadata.getDataUri());
        if(file!=null) dataSetInformation.setAttached(file.isFile());

        if (dataSetInformation.isAttached()) {
            dataSetInformation.setReadable(file.canRead());
            dataSetInformation.setByteSize(file.length());
            dataSetInformation.setModificationTime(new Date(file.lastModified()));
        }

        return dataSetInformation;
    }

    @Override
    public void store(MetaData metadata, InputStream content) throws BackendException {
        DataSetInformation dataSetInformation = gatherDataSetInformation(metadata);
        if (dataSetInformation.isAttached()) {
            throw new BackendException("Data set is already attached, won't overwrite.");
        }

        File file = getPathFromUri(metadata.getDataUri());




        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OutputStream read(MetaData metadata) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OutputStream delete(MetaData metadata) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
