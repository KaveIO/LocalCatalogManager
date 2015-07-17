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
package nl.kpmg.lcm.server.data.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendFileImpl;
import nl.kpmg.lcm.server.backend.BackendHDFSImpl;
import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.dao.BackendDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Crude service to work with backends.
 *
 * @author mhoekstra
 */
public class BackendService {

    @Autowired
    private BackendDao backendDao;

    /**
     * Get a storage backend based on a MetaData object.
     *
     * This will use the dataUri to retrieve the appropriate Backend.
     *
     * @param metadata of which the dataUri is used
     * @return the requested backend
     * @throws nl.kpmg.lcm.server.data.service.ServiceException
     */
    public final Backend getBackend(final MetaData metadata) {
        return getBackend(metadata.getDataUri());
    }

    /**
     * Get a storage backend based on a URI.
     *
     * This is a quickly hacked implementation. This should be changed to a annotation
     * driven implementation which will query the database for data backends with
     * their appropriate configuration.
     *
     * @param uri the URI to interpret
     * @return the requested backend
     * @throws nl.kpmg.lcm.server.data.service.ServiceException
     */
    public final Backend getBackend(final String uri) {
        String scheme;

        try {
            URI parsedUri = new URI(uri);
            scheme = parsedUri.getScheme();

            switch (scheme) {
                case "file":
                    if (parsedUri.getHost() != null)  {
                       return new BackendFileImpl(backendDao.getByName(parsedUri.getHost()));
                    } else {
                       return new BackendFileImpl(backendDao.getByName(parsedUri.getAuthority()));
                    }
                case "hdfs":
                    if (parsedUri.getHost() != null)  {
                        return new BackendHDFSImpl(backendDao.getByName(parsedUri.getHost()));
                    } else {
                        return new BackendHDFSImpl(backendDao.getByName(parsedUri.getAuthority()));
                    }
                default : return null;
            }

        } catch (URISyntaxException ex) {
            Logger.getLogger(BackendService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
