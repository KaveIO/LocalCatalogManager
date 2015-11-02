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
package nl.kpmg.lcm.server.rest.client.types;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import javax.ws.rs.core.Link;
import nl.kpmg.lcm.server.LinksSerializer;
import nl.kpmg.lcm.server.rest.Client;
import nl.kpmg.lcm.server.rest.client.Version0;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

/**
 *
 * @author mhoekstra
 */
public class ClientRepresentation {

    /**
     * The links to the various controllers in the system.
     */
    @InjectLinks({
        @InjectLink(
                resource = Client.class,
                style = InjectLink.Style.ABSOLUTE,
                rel = "login",
                method = "login",
                type = "application/nl.kpmg.lcm.server.rest.client.types.LoginRequest+json"
        ),
        @InjectLink(
                resource = Client.class,
                style = InjectLink.Style.ABSOLUTE,
                rel = "logout",
                method = "logout"
        ),
        @InjectLink(
                resource = Version0.class,
                style = InjectLink.Style.ABSOLUTE,
                rel = "version.0",
                method = "getIndex"
        )
    })
    private List<Link> links;

    /**
     * @return the list of Links
     */
    @JsonSerialize(using = LinksSerializer.class)
    public final List<Link> getLinks() {
        return links;
    }
}
