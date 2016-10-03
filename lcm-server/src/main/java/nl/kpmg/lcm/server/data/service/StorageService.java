/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package nl.kpmg.lcm.server.data.service;

import jersey.repackaged.com.google.common.collect.Lists;

import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendCsvImpl;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.Storage;
import nl.kpmg.lcm.server.data.dao.StorageDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.backend.BackendFactory;
import nl.kpmg.lcm.server.backend.exception.BackendNotImplementedException;

/**
 * Crude service to work with backends.
 *
 * @author mhoekstra
 */
@Service
public class StorageService {

    @Autowired
    private StorageDao storageDao;

    @Autowired
    private BackendFactory backendFactory;

    public List<Storage> findAll() {
        return Lists.newLinkedList(storageDao.findAll());
    }

    public StorageDao getStorageDao() {
        return storageDao;
    }

    /**
     * Get a storage backend based on a MetaData object.
     *
     * This will use the dataUri to retrieve the appropriate Backend.
     *
     * @param metadata of which the dataUri is used
     * @return the requested backend
     * @throws nl.kpmg.lcm.server.data.service.ServiceException
     */
    public final Backend getBackend(final MetaData metadata) throws BackendNotImplementedException {
        return getBackend(metadata.getDataUri());
    }

    /**
     * Get a storage backend based on a URI.
     *
     * This is a quickly hacked implementation. This should be changed to a
     * annotation driven implementation which will query the database for data
     * backends with their appropriate configuration.
     * @param uri the URI to interpret
     * @return the requested backend
     * @throws nl.kpmg.lcm.server.data.service.ServiceException
     */
    public final Backend getBackend(final String uri) throws BackendNotImplementedException {
        if (uri == null || uri.isEmpty()) {
            return null;
        }

        try {
            URI parsedUri = new URI(uri);
            String scheme = parsedUri.getScheme();

            String storagePath = parsedUri.getHost() != null ? parsedUri.getHost() : parsedUri.getAuthority();
            Storage storage = storageDao.findOne(storagePath);
            
            if(storage ==  null) {
                throw new IllegalStateException("Error! Unable to find Storage for the given metadata!");
            }

            Backend backend = backendFactory.createBackend(scheme, storage);

            return backend;

        } catch (URISyntaxException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
