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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.metadata.MetaData;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;

/**
 *
 * @author jpavel
 */
public class BackendHDFSImpl extends AbstractBackend {

    /**
     * Address of the HDFS server.
     *
     * @param storagePath is the server address.
     */
    private final String storagePath;

    /**
     * Default constructor.
     *
     * @param storagePath is the server address.
     */
    public BackendHDFSImpl(final String storagePath) {
        this.storagePath = storagePath;
    }

    /**
     * Returns scheme supported by URI for this backend.
     *
     * @return "hdfs" string
     */
    @Override
    protected final String getSupportedUriSchema() {
        return "hdfs";
    }

    /**
     * Returns a {@link FileSystem} specified by the URI. It checks if the URI
     * exists and if it uses "hdfs" protocol.
     *
     * @param uri is identifier of a file/directory at the HDFS backend
     * @return {
     * @FileSystem} instance connected with a location specified by the URI
     * @throws BackendException if no URI is specified, if it is using wrong
     * schema or if it is not possible to parse it correctly.
     * @throws BackendException
     * @throws IOException if it is not possible to connect to the location
     * specified by the URI
     */
    private FileSystem getFS() throws BackendException, IOException {

        Configuration conf = new Configuration();
        conf.set("fs.default.name", storagePath);
        FileSystem file = FileSystem.get(conf);
        return file;
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
        String uri = metadata.getDataUri();
        DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setUri(uri);

        try (FileSystem file = getFS()) {
            if (file != null && uri != null) {
                Path filePath = new Path(uri);
                dataSetInformation.setAttached(file.isFile(filePath));

                if (dataSetInformation.isAttached()) {
                    FileStatus fs = file.getFileStatus(filePath);
                    // check permission of the world
                    FsPermission perms = fs.getPermission();
                    FsAction otherAction = perms.getOtherAction();
                    boolean canRead = otherAction.implies(FsAction.READ);
                    dataSetInformation.setReadable(canRead);
                    dataSetInformation.setByteSize(fs.getLen());
                    dataSetInformation.setModificationTime(new Date(fs.getModificationTime()));
                }
            } else {
                throw new BackendException("URI not pointing to a file");
            }
        }
        catch (IOException ex) {
            Logger.getLogger(BackendHDFSImpl.class.getName()).log(Level.SEVERE, "Cannot reach the location " + uri, ex);
        }
        return dataSetInformation;
    }

    /**
     * Writes an input stream to the file specified in the {
     *
     * @MetaData}.
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

        try (FileSystem file = getFS()) {
            Path filePath = new Path(metadata.getDataUri());
            try (FSDataOutputStream fos = file.create(filePath, false)) {
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
        catch (IOException ex) {
            Logger.getLogger(BackendHDFSImpl.class.getName())
                    .log(Level.SEVERE, "Cannot reach the location " + metadata.getDataUri(), ex);
        }
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
