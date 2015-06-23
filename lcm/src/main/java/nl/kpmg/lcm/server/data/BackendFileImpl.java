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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

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

    /**
     * Writes an input stream to the file specifed in the {@MetaData}.
     * 
     * @param metadata should contain valid destination URI
     * @param content is a stream that should be stored
     * @throws BackendException if the URI in metadata points to the existing file
     */
    @Override
    public final void store(final MetaData metadata, final InputStream content) throws BackendException {
        DataSetInformation dataSetInformation = gatherDataSetInformation(metadata);
        if (dataSetInformation.isAttached()) {
            throw new BackendException("Data set is already attached, won't overwrite.");
        }

        File file = getPathFromUri(metadata.getDataUri());
        try (FileOutputStream fos = new FileOutputStream(file)) {
          int copied = IOUtils.copy(content, fos);
          Logger.getLogger(BackendFileImpl.class.getName())
             .log(Level.INFO, "{0} bytes written", copied);
        } catch (IOException ex) {
             Logger.getLogger(BackendFileImpl.class.getName())
             .log(Level.SEVERE, "Couldn't find path: " + metadata.getDataUri(), ex);
        }
    }

    /**
     * Returns an output stream with a content of a file that is specified by
     * metadata argument. {@link MetaData} needs to contain valid URI of a file.
     * 
     * @param metadata MetaData with URI of the data
     * @return OutputStream with the data file content
     * @throws BackendException if the metadata does not contain valid URI of a file
     */
    @Override
    public final OutputStream read(final MetaData metadata) throws BackendException {
        DataSetInformation dataSetInformation = gatherDataSetInformation(metadata);
        if (!dataSetInformation.isAttached()) {
            throw new BackendException("No dataset attached.");
        }
        File file = getPathFromUri(metadata.getDataUri());
        OutputStream os = null;
        try (FileInputStream fis = new FileInputStream(file)) {
            int readBytes = IOUtils.copy(fis, os);
            Logger.getLogger(BackendFileImpl.class.getName())
             .log(Level.INFO, "{0} bytes read", readBytes);
        } catch (IOException ex) {
             Logger.getLogger(BackendFileImpl.class.getName())
             .log(Level.SEVERE, "Couldn't read path: " + metadata.getDataUri(), ex);
        }
        return os;
    }

    @Override
    public OutputStream delete(MetaData metadata) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
