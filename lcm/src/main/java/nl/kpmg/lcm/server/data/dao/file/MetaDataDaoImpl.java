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
package nl.kpmg.lcm.server.data.dao.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import nl.kpmg.lcm.server.data.dao.DaoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Link;
import nl.kpmg.lcm.server.JacksonJsonProvider;
import nl.kpmg.lcm.server.NotFilteringFilterProvider;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;

/**
 * Implementation of a file based MetaData DAO.
 */
public class MetaDataDaoImpl implements MetaDataDao {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(MetaDataDaoImpl.class.getName());

    /**
     * Path where the metaData is stored.
     */
    private final File storage;

    /**
     * Object mapper used to serialize and de-serialize the metaData.
     */
    private final ObjectMapper mapper;

    /**
     * @param storagePath The path where the metaData is stored
     * @throws DaoException when the storagePath doesn't exist
     */
    public MetaDataDaoImpl(final String storagePath) throws DaoException {
        storage = new File(storagePath);

        JacksonJsonProvider jacksonJsonProvider = new JacksonJsonProvider();
        mapper = jacksonJsonProvider.getContext(MetaData.class);

        if (!storage.isDirectory() || !this.storage.canWrite()) {
            throw new DaoException(String.format(
                    "The storage path %s is not a directory or not writable.", storage.getAbsolutePath()));
        }
    }

    private File getMetaDataFile(String name, String versionNumber) {
        return new File(String.format("%s/%s/%s", storage, name, versionNumber));
    }

    private File getMetaDataFolder(String name) {
        return new File(String.format("%s/%s", storage, name));
    }

    @Override
    public List<MetaData> getAll() {
        String[] allMetaDataNames = storage.list();
        LinkedList<MetaData> result = new LinkedList();

        for (String metaDataName : allMetaDataNames) {
            MetaData metaData = getByName(metaDataName);
            if (metaData != null) {
                result.add(metaData);
            }
        }
        return result;
    }

    @Override
    public MetaData getByName(String name) {
        File metaDataFolder = getMetaDataFolder(name);
        if (metaDataFolder.isDirectory()) {
            String[] versions = metaDataFolder.list();

            Arrays.sort(versions);
            String head = versions[versions.length - 1];
            return getByNameAndVersion(name, head);
        }
        return null;
    }

    @Override
    public MetaData getByNameAndVersion(String name, String versionNumber) {
        try {
            MetaData metaData = mapper.readValue(getMetaDataFile(name, versionNumber), MetaData.class);
            metaData.setName(name);
            metaData.setVersionNumber(versionNumber);
            return metaData;
        }
        catch (IOException ex) {
            Logger.getLogger(MetaDataDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public void update(MetaData metadata) {
        String name = metadata.getName();
        String version = metadata.getVersionNumber();
        MetaData tmpData = getByNameAndVersion(name, version);
        if (tmpData != null) {
            if (metadata.containsKey("Duplicates")) {
                List<MetaData> mlist = metadata.getDuplicates();
                tmpData.put("Duplicates", mlist);
                try {
                    mapper.writeValue(getMetaDataFile(name, version), tmpData);
                } catch (IOException ex) {
                    Logger.getLogger(MetaDataDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            LOGGER.warning("MetaData entered is NULL or doesn't have Duplicate field");
        }
    }

    @Override
    public void persist(MetaData metadata) {
        String name = metadata.getName();
        String versionNumber = "0";
        MetaData previousVersionMetaData = getByName(name);

        if (previousVersionMetaData == null) {
            getMetaDataFolder(name).mkdir();
        } else {
            versionNumber = previousVersionMetaData.getVersionNumber();

            if (versionNumber == null) {
                LOGGER.warning("Previous version found be no version number could be parsed.");
                versionNumber = "0";
                /**
                 * @TODO quick and dirty. Should throw
                 */
            } else {
                int previousVersionNumber = Integer.parseInt(versionNumber);
                versionNumber = "" + (previousVersionNumber + 1);
            }
        }

        try {
            metadata.setVersionNumber(versionNumber);
            mapper.writeValue(getMetaDataFile(name, versionNumber), metadata);
        }
        catch (IOException ex) {
            Logger.getLogger(MetaDataDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void delete(MetaData metadata) {
        File metaDataFolder = getMetaDataFolder(metadata.getName());
        for (File versionFile : metaDataFolder.listFiles()) {
            versionFile.delete();
        }
        metaDataFolder.delete();
    }
}
