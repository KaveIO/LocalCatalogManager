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
package nl.kpmg.lcm.server.metadata.storage.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.JacksonJsonProvider;
import nl.kpmg.lcm.server.metadata.MetaData;
import nl.kpmg.lcm.server.metadata.storage.MetaDataDao;

/**
 *
 * @author mhoekstra
 */
public class MetaDataDaoImpl implements MetaDataDao {
    private static final Logger logger = Logger.getLogger(MetaDataDaoImpl.class.getName());
    private final String storagePath = "./metadata";
    private final ObjectMapper mapper;

    public MetaDataDaoImpl() {
        JacksonJsonProvider jacksonJsonProvider = new JacksonJsonProvider();
        mapper = jacksonJsonProvider.getContext(MetaData.class);
        
        /** @TODO remove this hack */
        new File(storagePath).mkdir();
    }

    private File getMetaDataFile(String name, String versionNumber) {
        return new File(String.format("%s/%s/%s", storagePath, name, versionNumber));
    }
    
    private File getMetaDataFolder(String name) {
        return new File(String.format("%s/%s", storagePath, name));
    }
    
    @Override
    public List<MetaData> getAll() {
        String[] allMetaDataNames = new File(storagePath).list();
        LinkedList<MetaData> result = new LinkedList();
        
        for (int i = 0; i < allMetaDataNames.length; i++) {
            MetaData metaData = getByName(allMetaDataNames[i]);
            if (metaData != null) {
                result.add(metaData);
            }
        }
        
        return result;
    }

    @Override
    public MetaData getByName(String name) {
        File metaDataFolder = getMetaDataFolder(name);
        String[] list = metaDataFolder.list();
        
        if (!metaDataFolder.isDirectory()) {
            metaDataFolder.mkdir();
        }
        
        if (list == null) {
            return null;
        } else {
            /** @TODO this needs be sorted currently we don't really listen to versions */
            String versionNumber = list[list.length - 1];
            return getByNameAndVersion(name, versionNumber);
        }
    }

    @Override
    public MetaData getByNameAndVersion(String name, String versionNumber) {
        try {
            MetaData metaData = mapper.readValue(getMetaDataFile(name, versionNumber), MetaData.class);
            metaData.setName(name);
            metaData.setVersionNumber(versionNumber);
            return mapper.readValue(getMetaDataFile(name, versionNumber), MetaData.class);
        } catch (IOException ex) {
            Logger.getLogger(MetaDataDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public void persist(MetaData metadata) {
        String name = metadata.getName();
        String versionNumber = "0";
        MetaData previousVersionMetaData = getByName(name);

        if (previousVersionMetaData == null) {

        } else {
            versionNumber = previousVersionMetaData.getVersionNumber();
            
            if (versionNumber == null) {
                logger.warning("Previous version found be no version number could be parsed.");
                versionNumber = "0"; /** @TODO quick and dirty. Should throw */
            } else {
                int previousVersionNumber = Integer.parseInt(versionNumber);
                versionNumber = "" + (previousVersionNumber + 1);
            }
        }
            
        try {
            mapper.writeValue(getMetaDataFile(name, versionNumber), metadata);
        } catch (IOException ex) {
            Logger.getLogger(MetaDataDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void delete(MetaData metadata) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteVersion(MetaData metadata) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void getVersionNumber() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
