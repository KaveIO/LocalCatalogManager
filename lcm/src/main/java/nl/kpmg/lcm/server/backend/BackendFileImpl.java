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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.data.BackendModel;
import org.apache.commons.io.IOUtils;

import nl.kpmg.lcm.server.data.MetaData;

/**
 *
 * @author mhoekstra
 */
public class BackendFileImpl extends AbstractBackend {

    /**
     * Location of the data storage on the local file system.
     *
     * @param storagePath is the directory on a local backend
     */
    private final File storagePath;

//    /**
//     * Default constructor.
//     *
//     * @param storagePath is the directory on a local backend
//     */
//    public BackendFileImpl(final File storagePath) {
//        this.storagePath = storagePath;
//    }

    public BackendFileImpl(final BackendModel backend) {
        this.storagePath = new File((String) backend.getOptions().get("storagePath"));
    }


    /**
     * Returns a {@link File} specified by the URI. It checks if the URI exists
     * and if it uses "file" protocol.
     *
     * @param uri is identifier of a local file/directory
     * @return File pointed at by the URI
     * @throws BackendException if no URI is specified
     */
    private File getPathFromUri(final String uri) throws BackendException {
        URI dataUri;
        /**
         * @TODO Should we issue a warning via logger or exception in this case?
         */
        if (uri != null) {
            dataUri = parseUri(uri);

            String filePath = dataUri.getPath();
            /**
             * @TODO This is super scary. we should check if the resulting path
             * is still within storagePath
             */
            return new File(filePath);
        } else {
            throw new BackendException("No URI specified.");
        }
    }

    /**
     * Returns scheme supported by URI for this backend.
     *
     * @return "file" string
     */
    @Override
    protected final String getSupportedUriSchema() {
        return "file";
    }

    /**
     * Returns information about dataset mentioned in the metadata. It checks if
     * the referenced data exist and can be accessed. It also gathers
     * information about the size and modification time.
     *
     * @param metadata is investigated {@link MetaData} object
     * @return filled {@link DataSetInformation} object
     * @throws BackendException
     */
    @Override
    public final DataSetInformation gatherDataSetInformation(final MetaData metadata) throws BackendException {
        File file = getPathFromUri(metadata.getDataUri());

        DataSetInformation dataSetInformation = new DataSetInformation();

        dataSetInformation.setUri(metadata.getDataUri());
        if (file != null) {
            dataSetInformation.setAttached(file.isFile());
        } else {
            throw new BackendException("URI not pointing to a file");
        }

        if (dataSetInformation.isAttached()) {
            dataSetInformation.setReadable(file.canRead());
            dataSetInformation.setByteSize(file.length());
            dataSetInformation.setModificationTime(new Date(file.lastModified()));
        }

        return dataSetInformation;
    }

    /**
     * Writes an input stream to the file specified in the {@link MetaData}.
     *
     * @param metadata should contain valid destination URI
     * @param content is a stream that should be stored
     * @throws BackendException if the URI in metadata points to the existing
     * file
     */
    @Override
    public final void store(final MetaData metadata, final InputStream content) throws BackendException {
        DataSetInformation dataSetInformation = gatherDataSetInformation(metadata);
        if (dataSetInformation.isAttached()) {
            throw new BackendException("Data set is already attached, won't overwrite.");
        }

        File file = getPathFromUri(metadata.getDataUri());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // this works for files < 2 GB. Otherwise the copied is -1.
            int copied = IOUtils.copy(content, fos);
            Logger.getLogger(BackendFileImpl.class.getName())
                    .log(Level.INFO, "{0} bytes written", copied);
        }
        catch (IOException ex) {
            Logger.getLogger(BackendFileImpl.class.getName())
                    .log(Level.SEVERE, "Couldn't find path: " + metadata.getDataUri(), ex);
        }
    }

    /**
     * Returns an input stream with a content of a file that is specified by
     * metadata argument. Returns null if it is not possible to open the file.
     * {@link MetaData} needs to contain valid URI of a file.
     *
     * @param metadata MetaData with URI of the data
     * @return InputStream with the data file content
     * @throws BackendException if the metadata does not contain valid URI of a
     * file
     */
    @Override
    public final InputStream read(final MetaData metadata) throws BackendException {
        DataSetInformation dataSetInformation = gatherDataSetInformation(metadata);
        if (!dataSetInformation.isAttached()) {
            throw new BackendException("No dataset attached.");
        }
        File file = getPathFromUri(metadata.getDataUri());
        try {
            InputStream is = new FileInputStream(file);
            return is;
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(BackendFileImpl.class.getName())
                    .log(Level.SEVERE, "Did not find file: " + metadata.getDataUri()
                            + ". Returning null.", ex);
        }
        return null;
    }

    /**
     * Deletes the file specified in the {@link MetaData}.
     *
     * @param metadata {@link MetaData} with URI of the data
     * @return true if delete is successful, false otherwise
     * @throws BackendException if the metadata does not contain valid URI of a
     * file
     */
    @Override
    public final boolean delete(final MetaData metadata) throws BackendException {
        DataSetInformation dataSetInformation = gatherDataSetInformation(metadata);
        if (!dataSetInformation.isAttached()) {
            throw new BackendException("No dataset attached.");
        }
        File file = getPathFromUri(metadata.getDataUri());
        boolean success = file.delete();
        if (success) {
            Logger.getLogger(BackendFileImpl.class.getName())
                    .log(Level.INFO, "Delete successful.");
        } else {
            Logger.getLogger(BackendFileImpl.class.getName())
                    .log(Level.SEVERE, "Deletion of file: {0} failed.", metadata.getDataUri());
        }
        return success;
    }

    /**
     *
     * @return storagePath as File (useful for testing)
     */
    public File getStoragePath() {
        return storagePath;
    }



}
