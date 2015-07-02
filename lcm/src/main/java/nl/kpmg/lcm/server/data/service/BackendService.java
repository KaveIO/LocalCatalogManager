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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.kpmg.lcm.server.backend.Backend;
import nl.kpmg.lcm.server.backend.BackendFileImpl;
import nl.kpmg.lcm.server.data.MetaData;

/**
 * Crude service to work with backends.
 *
 * @author mhoekstra
 */
public class BackendService {

    /**
     * Get a storage backend based on a MetaData object.
     *
     * This will use the dataUri to retrieve the appropriate Backend.
     *
     * @param metadata of which the dataUri is used
     * @return the requested backend
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
     */
    public final Backend getBackend(final String uri) {
        try {
            URI parsedUri = new URI(uri);
            String scheme = parsedUri.getScheme();

            switch (scheme) {
                case "file":
                    return new BackendFileImpl(new File("/"));
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(BackendService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
