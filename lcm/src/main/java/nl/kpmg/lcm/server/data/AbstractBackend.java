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

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author mhoekstra
 */
public abstract class AbstractBackend implements Backend {

    protected abstract String getSupportedUriSchema();

    protected URI parseUri(String uri) throws BackendException {
        try {
            URI parsedUri = new URI(uri);
            if(parsedUri.getScheme()== null){
                // Strangely enough, the URI constructor allows URI instances without scheme
                throw new BackendException(String.format(
                        "No scheme supplied in uri \"%s\". Please use the supported uri schema (%s)", 
                        uri, getSupportedUriSchema()));
            }
            if (!parsedUri.getScheme().equals(getSupportedUriSchema())) {
                throw new BackendException(String.format(
                        "Detected uri schema (%s) doesn't match with this backends supported uri schema (%s)",
                        parsedUri.getScheme(), getSupportedUriSchema()));
            }
            return parsedUri;
        } catch (URISyntaxException ex) {
            throw new BackendException(String.format("Failure while trying to parse URI '%s'", uri), ex);
        }
    }
}
